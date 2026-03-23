# portal/auth-center Python 自动化测试

这套测试遵循 `agents.md` 中“自动化测试”的约定：

- 使用 `python3 + requests`
- 测试账号、组织、host、验证码处理方式集中配置在 `settings.py`
- 先统一登录多组账号，再按组织和权限映射执行流程型 API 测试

当前已提供两组首批流程：

- `test_auth_flow.py`
  - 登录所有已配置账号
  - 校验登录返回 token、tenantId、userId
- `test_access_token_flow.py`
  - 租户默认管理员执行 AK 的增删改查
  - 无权限账号验证越权失败

## 目录

```text
portal/test/auth_center/
├── common/
├── suites/
├── requirements.txt
├── run.py
└── settings.py
```

## 使用

1. 编辑 `settings.py`，填入环境地址、测试账号、组织 ID。
2. 安装依赖：

```bash
python3 -m pip install -r portal/test/auth_center/requirements.txt
```

3. 执行测试：

```bash
python3 portal/test/auth_center/run.py
```

## 验证码

登录前会调用 `/api/portal/v1/auth/captcha` 获取验证码图片，并保存到：

- `portal/test/auth_center/.artifacts/captcha/`

脚本会提示你手动输入验证码内容，再继续登录。
