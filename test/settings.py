from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
ARTIFACT_DIR = BASE_DIR / ".artifacts"
CAPTCHA_DIR = ARTIFACT_DIR / "captcha"

HOST = "http://127.0.0.1:20001"
APP_ID = "portal"
REQUEST_TIMEOUT = 15

# 账号、组织和额外请求头集中放在这里维护。
# 如果你的测试入口是网关，一般只需要 token + X-APP-ID + X-ORG-ID。
# 如果你的测试入口是 auth-center 服务本身，而不是网关，可在 extra_headers 中补齐
# X-USER-ID / X-TENANT-ID / X-USER-NAME / X-ADMIN-FLAG 等上下文头。
ACCOUNTS = {
    "super_admin": {
        "login_field": "account",
        "principal": "",
        "password": "",
        "tenant_id": "",
        "org_ids": [""],
        "extra_headers": {},
    },
    "granted_user": {
        "login_field": "account",
        "principal": "",
        "password": "",
        "tenant_id": "",
        "org_ids": [""],
        "extra_headers": {},
    },
    "forbidden_user": {
        "login_field": "account",
        "principal": "",
        "password": "",
        "tenant_id": "",
        "org_ids": [""],
        "extra_headers": {},
    },
}
