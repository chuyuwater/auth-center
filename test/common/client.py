from __future__ import annotations

import base64
import mimetypes
import os
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import requests
from requests import Response

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
        body = self.read_json(
            response,
            context=(
                f"{account.name} 登录响应不是合法 JSON; "
                f"login_field={account.login_field}, principal={account.principal}, tenant_id={account.tenant_id or '<empty>'}"
            ),
        )
        if response.status_code == 400 and body.get("status") == 100:
            raise RuntimeError(
                f"{account.name} 登录命中了多租户选择，请在 settings.py 里补 tenant_id，候选租户: {body.get('data')}"
            )
        if response.status_code != 200:
            raise RuntimeError(
                f"{account.name} 登录失败: {self.describe_response(response, body_override=body)}"
            )
        if body.get("status") != 0:
            raise RuntimeError(
                f"{account.name} 登录失败: {self.describe_response(response, body_override=body)}"
            )

        data = body["data"]
        return SessionState(
            account=account,
            token=data["token"],
            user_id=data["userId"],
            tenant_id=data["tenantId"],
            raw_login_data=data,
        )

    def logout(self, session_state: SessionState) -> None:
        response = self.request(
            "POST",
            "/api/portal/v1/auth/logout",
            session_state=session_state,
        )
        body = self.read_json(response, context=f"{session_state.account.name} 登出响应不是合法 JSON")
        if response.status_code != 200 or body.get("status") != 0:
            raise RuntimeError(
                f"{session_state.account.name} 登出失败: {self.describe_response(response, body_override=body)}"
            )

    def get_captcha(self, account_name: str) -> dict[str, str]:
        response = self.http.get(self.url("/api/portal/v1/auth/captcha"), timeout=REQUEST_TIMEOUT)
        body = self.read_json(response, context=f"{account_name} 获取验证码响应不是合法 JSON")
        if response.status_code != 200 or body.get("status") != 0:
            raise RuntimeError(f"获取验证码失败: {self.describe_response(response, body_override=body)}")

        data = body["data"]
        image_path = self.save_captcha_image(account_name, data["base64"])
        self.show_captcha_image(image_path)
        try:
            code = input(f"[{account_name}] 请输入验证码 {image_path}: ").strip()
        finally:
            self.cleanup_captcha_image(image_path)
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

        url = self.url(path)
        try:
            return self.http.request(
                method=method,
                url=url,
                params=params,
                json=json_body,
                headers=final_headers,
                timeout=REQUEST_TIMEOUT,
            )
        except requests.RequestException as exc:
            raise RuntimeError(
                "HTTP 请求失败: "
                f"method={method}, url={url}, params={params}, json={json_body}, "
                f"headers={self.mask_headers(final_headers)}, error={exc}"
            ) from exc

    def url(self, path: str) -> str:
        return f"{self.base_url}{path}"

    def show_captcha_image(self, image_path: Path) -> None:
        if sys.platform != "darwin":
            return
        imgcat = shutil.which("imgcat")
        if not imgcat:
            return
        try:
            subprocess.run([imgcat, os.fspath(image_path)], check=False)
        except OSError:
            pass

    def cleanup_captcha_image(self, image_path: Path) -> None:
        try:
            image_path.unlink(missing_ok=True)
        except OSError:
            pass

    def read_json(self, response: Response, *, context: str) -> dict[str, Any]:
        try:
            body = response.json()
        except ValueError as exc:
            raise RuntimeError(f"{context}: {self.describe_response(response)}") from exc
        if not isinstance(body, dict):
            raise RuntimeError(f"{context}: {self.describe_response(response, body_override=body)}")
        return body

    def describe_response(self, response: Response, *, body_override: Any | None = None) -> str:
        body = body_override
        if body is None:
            try:
                body = response.json()
            except ValueError:
                body = response.text
        text = body if isinstance(body, str) else repr(body)
        text = text.strip()
        if len(text) > 1000:
            text = text[:1000] + "...<truncated>"
        return (
            f"method={response.request.method}, url={response.request.url}, "
            f"http={response.status_code}, content_type={response.headers.get('Content-Type', '')}, "
            f"body={text}"
        )

    def mask_headers(self, headers: dict[str, str]) -> dict[str, str]:
        masked = dict(headers)
        if "X-AUTH-TOKEN" in masked:
            masked["X-AUTH-TOKEN"] = "<masked>"
        return masked
