from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
ARTIFACT_DIR = BASE_DIR / ".artifacts"
CAPTCHA_DIR = ARTIFACT_DIR / "captcha"

HOST = "https://portal-dev.chuyuwater.cn"
APP_ID = "portal"
REQUEST_TIMEOUT = 15

# 账号、组织和额外请求头集中放在这里维护。
# 本项目当前存在两条测试线：
# 1. 固定账号线：使用这里预先配置好的真实账号、真实组织、真实授权关系，
#    验证 portal 授权体系、组织切换和真实业务接口。
# 2. 临时数据线：测试过程中临时创建租户、组织、用户、角色、权限资源，
#    验证管理类 CRUD 和授权/撤权闭环。
#
# 当前固定账号的约束如下：
# - super_admin:
#   1. 拥有平台级最高权限，可创建租户、授权应用、管理资源和权限单元。
# - granted_user:
#   1. 用于验证“同一账号切换不同组织时，权限和数据范围不同”。
#   2. org_ids 至少配置两个组织，顺序有语义：
#      - org_ids[0]: 预期有授权，用于验证“应能看到/应有权限”
#      - org_ids[1]: 预期无授权，用于验证“应看不到/应无权限”
#   3. 后续如果要测更复杂的本级/下级/本下范围，可以继续往后补更多组织。
# - forbidden_user:
#   1. 默认不应拥有被测试的目标权限。
#   2. 主要用于“先授权 -> 重新登录 -> 验证生效 -> 撤权 -> 重新登录 -> 验证失效”。
#
# 权限缓存不是实时失效的。凡是涉及以下动作，测试里原则上都应 logout 后重新 login 再验证：
# - 权限单元封装权限
# - 权限单元封装用户
# - 撤销权限单元授权
# - 租户授权/撤销应用
#
# 如果你的测试入口是网关，一般只需要 token + X-APP-ID + X-ORG-ID。
# 如果你的测试入口是 auth-center 服务本身，而不是网关，可在 extra_headers 中补齐
# X-USER-ID / X-TENANT-ID / X-USER-NAME / X-ADMIN-FLAG 等上下文头。
ACCOUNTS = {
    "super_admin": {
        "login_field": "account",
        "principal": "18502710984",
        "password": "Chuyu@2026",
        "tenant_id": "",
        "org_ids": ["0-ORG-000001"],
        "extra_headers": {},
    },
    "granted_user": {
        "login_field": "account",
        "principal": "17712345678",
        "password": "17712345678",
        "tenant_id": "",
        # 至少两个组织：
        # org_ids[0] = 有管理消息权限的组织
        # org_ids[1] = 无管理消息权限的组织
        "org_ids": ["0-ORG-000001", "0-ORG-000015"],
        "extra_headers": {},
    },
    "forbidden_user": {
        "login_field": "account",
        "principal": "17912345678",
        "password": "17912345678",
        "tenant_id": "",
        # 默认组织即可。该账号默认不应拥有任何权限
        "org_ids": ["0-ORG-000001"],
        "extra_headers": {},
    },
}
