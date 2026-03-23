from __future__ import annotations

import base64
import mimetypes
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import requests

from settings import ACCOUNTS, APP_ID, CAPTCHA_DIR, HOST, REQUEST_TIMEOUT


@dataclass(frozen=True)
class AccountConfig:
    name: str
    login_field: str
    principal: str
    password: str
    tenant_id: str
    org_ids: list[str]
    extra_headers: dict[str, str]

    @property
    def default_org_id(self) -> str:
        return self.org_ids[0] if self.org_ids else ""


@dataclass
class SessionState:
    account: AccountConfig
    token: str
    user_id: str
    tenant_id: str
    raw_login_data: dict[str, Any]


class AuthCenterClient:
    def __init__(self) -> None:
        self.base_url = HOST.rstrip("/")
        self.http = requests.Session()
        self.http.headers.update({"Accept": "application/json"})

    def build_account(self, name: str) -> AccountConfig:
        raw = ACCOUNTS[name]
        login_field = raw["login_field"]
        principal = raw["principal"]
        password = raw["password"]
        if login_field not in {"account", "phone", "email"}:
            raise RuntimeError(f"{name} 的 login_field 非法: {login_field}")
        if not principal or not password:
            raise RuntimeError(f"{name} 账号配置不完整，请检查 settings.py")
        return AccountConfig(
            name=name,
            login_field=login_field,
            principal=principal,
            password=password,
            tenant_id=raw.get("tenant_id", ""),
            org_ids=list(raw.get("org_ids", [])),
            extra_headers=dict(raw.get("extra_headers", {})),
        )

    def login(self, account: AccountConfig) -> SessionState:
        captcha = self.get_captcha(account.name)
        payload = {
            account.login_field: account.principal,
            "password": account.password,
            "captchaId": captcha["id"],
            "captchaCode": captcha["code"],
        }
        if account.tenant_id:
            payload["tenantId"] = account.tenant_id

        response = self.http.post(
            self.url("/api/portal/v1/auth/login"),
            json=payload,
            timeout=REQUEST_TIMEOUT,
        )
        body = response.json()
        if response.status_code == 400 and body.get("status") == 100:
            raise RuntimeError(
                f"{account.name} 登录命中了多租户选择，请在 settings.py 里补 tenant_id，候选租户: {body.get('data')}"
            )
        if response.status_code != 200:
            raise RuntimeError(f"{account.name} 登录失败: http={response.status_code}, body={body}")
        if body.get("status") != 0:
            raise RuntimeError(f"{account.name} 登录失败: body={body}")

        data = body["data"]
        return SessionState(
            account=account,
            token=data["token"],
            user_id=data["userId"],
            tenant_id=data["tenantId"],
            raw_login_data=data,
        )

    def get_captcha(self, account_name: str) -> dict[str, str]:
        response = self.http.get(self.url("/api/portal/v1/auth/captcha"), timeout=REQUEST_TIMEOUT)
        body = response.json()
        if response.status_code != 200 or body.get("status") != 0:
            raise RuntimeError(f"获取验证码失败: http={response.status_code}, body={body}")

        data = body["data"]
        image_path = self.save_captcha_image(account_name, data["base64"])
        code = input(f"[{account_name}] 请输入验证码 {image_path}: ").strip()
        if not code:
            raise RuntimeError(f"{account_name} 未输入验证码")
        return {"id": data["id"], "code": code}

    def save_captcha_image(self, account_name: str, base64_text: str) -> Path:
        CAPTCHA_DIR.mkdir(parents=True, exist_ok=True)
        if "," in base64_text:
            header, encoded = base64_text.split(",", 1)
            mime_type = header.split(":", 1)[1].split(";", 1)[0]
        else:
            encoded = base64_text
            mime_type = "image/png"
        ext = mimetypes.guess_extension(mime_type) or ".png"
        image_path = CAPTCHA_DIR / f"{account_name}_{int(time.time())}{ext}"
        image_path.write_bytes(base64.b64decode(encoded))
        return image_path

    def request(
        self,
        method: str,
        path: str,
        *,
        session_state: SessionState | None = None,
        org_id: str | None = None,
        params: dict[str, Any] | None = None,
        json_body: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ):
        final_headers: dict[str, str] = {}
        if session_state is not None:
            final_headers["X-AUTH-TOKEN"] = session_state.token
            final_headers["X-APP-ID"] = APP_ID
            chosen_org_id = org_id if org_id is not None else session_state.account.default_org_id
            if chosen_org_id:
                final_headers["X-ORG-ID"] = chosen_org_id
            final_headers.update(session_state.account.extra_headers)
        if headers:
            final_headers.update(headers)

        return self.http.request(
            method=method,
            url=self.url(path),
            params=params,
            json=json_body,
            headers=final_headers,
            timeout=REQUEST_TIMEOUT,
        )

    def url(self, path: str) -> str:
        return f"{self.base_url}{path}"
