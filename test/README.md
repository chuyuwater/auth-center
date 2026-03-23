# portal/auth-center Python 自动化测试

这套测试遵循 `agents.md` 中“自动化测试”的约定：

- 使用 `python3 + requests`
- 测试账号、组织、host、验证码处理方式集中配置在 `settings.py`
- 先统一登录多组账号，再按组织和权限映射执行流程型 API 测试

## 测试策略

当前测试按两条线组织：

- 固定账号线
  - 使用 `settings.py` 中预先配置好的真实账号、真实组织、真实授权关系。
  - 目标是验证 portal 的授权体系、组织切换、数据权限范围，以及真实业务接口是否可用。
  - 这条线优先使用真实应用和真实权限点，当前默认就是 `portal`。
  - 如果某个权限最终依赖真实 URI 或真实资源绑定，仅靠 `perm-check` 不够，应直接调用真实 GET 接口验证。

- 临时数据线
  - 测试过程中临时创建租户、组织、用户、权限单元、应用或资源。
  - 目标是验证平台管理能力的 CRUD、授权闭环、撤权闭环和清理逻辑。
  - 这条线允许使用 `perm-check` 作为授权状态验证手段；如果接口本身已有真实受控 GET，也应优先补真实接口验证。

## 配置前置条件

`settings.py` 中当前至少要满足这些前提：

- `super_admin`
  - 拥有平台级最高权限，可创建租户、授权应用、维护资源和权限单元。
- `granted_user`
  - 至少配置两个组织。
  - `org_ids[0]` 预期有权限，用于验证“应能看到/应有权限”。
  - `org_ids[1]` 预期无权限，用于验证“应看不到/应无权限”。
- `forbidden_user`
  - 默认不应拥有目标权限。
  - 主要用于“先授权，再重新登录，再验权；撤权后重新登录，再确认权限消失”。

## 缓存处理原则

授权缓存不是实时失效的。以下场景默认要执行 `logout + login` 后再验证结果：

- 权限单元封装权限
- 权限单元封装用户
- 权限单元撤权
- 租户授权应用
- 租户撤销应用授权

当前已提供两组首批流程：

- `test_auth_flow.py`
  - 登录所有已配置账号
  - 校验登录返回 token、tenantId、userId
- `test_access_token_flow.py`
  - 租户默认管理员执行 AK 的增删改查
  - 无权限账号验证越权失败
- `test_app_resource_flow.py`
  - 应用的增删改查
  - 菜单节点的增删改查
  - 菜单下权限点及 API 绑定的增删改查
- `test_tenant_flow.py`
  - 租户的创建、列表、详情、更新、状态切换
  - 删除前置约束校验
- `test_org_user_flow.py`
  - 新租户默认管理员登录
  - 公司、部门、人员的增删改查
  - 组织删除前的用户关联约束校验
- `test_perm_unit_flow.py`
  - 权限单元分组、权限单元的增删改查
  - 基于 portal 应用封装权限点
  - 将 `forbidden_user` 封装进权限单元并重新登录验权
  - 撤销授权并恢复用户原始状态
- `test_msg_todo_flow.py`
  - 本人消息、本人待办查询
  - 切换组织后，本人消息/待办仍可见
  - 消息标记已读、本人消息删除
  - 待办状态更新、标记已读、本人待办删除
  - `granted_user` 切换组织后，对管理端消息/待办查询的数据权限校验
- `test_tenant_app_grant_flow.py`
  - 新建租户后为租户授权 `portal` 应用
  - 新建组织、临时用户、权限单元并授予 portal 权限
  - 通过 `perm-check` 验证授权生效
  - 撤销租户应用授权后重新登录，再验证权限失效
  - 最后按角色、人员、组织、租户顺序清理

## 目录

```text
portal/auth-center/test/
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
python3 -m pip install -r portal/auth-center/test/requirements.txt
```

3. 执行测试：

```bash
python3 portal/auth-center/test/run.py
```

## 当前覆盖

- 登录与验证码流程
- Access Token 管理
- 应用、菜单、权限资源管理
- 租户管理
- 组织与人员管理
- 权限单元管理、封装权限、封装用户、撤权恢复
- 消息与待办查询、本人查询、组织切换数据权限
- 租户授权应用、角色授权、撤销应用授权后权限失效

## 待补的真实 GET 授权验证清单

下面这些能力已经有基础测试，但仍建议进一步补充“真实受控 GET 接口”验证，而不是只依赖 `perm-check`：

- 权限单元授权后，对应 portal 真实页面资源或查询接口的访问结果
- 租户授权应用后，`/api/portal/v1/client/app`
  - 验证用户重新登录后是否能看到 `portal`
- 租户撤销应用授权后，`/api/portal/v1/client/app`
  - 验证用户重新登录后应用入口是否消失
- 租户授权应用后，`/api/portal/v1/client/res`
  - 验证用户在当前组织、当前应用下是否能拿到菜单树
- 租户撤销应用授权后，`/api/portal/v1/client/res`
  - 验证菜单树是否清空
- 更多真实受控查询接口
  - 例如消息管理、待办管理、用户查询、组织查询等已经受权限影响的 GET 接口
  - 原则上都应该逐步从“只看 `perm-check`”升级到“直接验证接口返回结果”

## 验证码

登录前会调用 `/api/portal/v1/auth/captcha` 获取验证码图片，并保存到：

- `portal/auth-center/test/.artifacts/captcha/`

脚本会提示你手动输入验证码内容，再继续登录。
