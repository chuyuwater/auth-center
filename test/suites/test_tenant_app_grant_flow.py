from __future__ import annotations

import time

from common.assertions import ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class TenantAppGrantFlowTestCase(BaseFlowTestCase):
    def test_tenant_app_grant_and_revoke_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("139", suffix)
        tenant_name = f"应用授权租户{suffix[-6:]}"

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_id = ""
        department_id = ""
        temp_user_id = ""
        perm_group_id = ""
        perm_unit_id = ""
        perm_grant_ids: list[str] = []
        temp_user_alias = f"tenant_app_user_{suffix}"
        tenant_admin_alias = f"tenant_admin_{suffix}"
        tenant_admin = None

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": tenant_name,
                "shortName": f"租户{suffix[-4:]}",
                "logo": "",
                "memo": "tenant app grant flow",
                "contactUser": "租户管理员",
                "contactPhone": tenant_phone,
            },
        )
        ensure_http_status(create_tenant_response, 200)
        tenant = ensure_api_status(create_tenant_response.json())
        tenant_id = tenant["id"]
        tenant_root_org_id = f"{tenant_id}-ORG-000001"

        try:
            grant_app_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app",
                session_state=super_admin_session,
                json_body={
                    "tenantId": tenant_id,
                    "appId": "portal",
                    "grantAll": True,
                    "permIds": [],
                },
            )
            ensure_http_status(grant_app_response, 200)
            ensure_api_status(grant_app_response.json())

            list_grant_apps_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/grant/app/{tenant_id}",
                session_state=super_admin_session,
            )
            ensure_http_status(list_grant_apps_response, 200)
            grant_apps = ensure_api_status(list_grant_apps_response.json())
            portal_grant = next((item for item in grant_apps if item["appId"] == "portal"), None)
            self.assertIsNotNone(portal_grant, "租户应用授权列表中未找到 portal")
            tenant_app_grant_id = portal_grant["grantId"]

            tenant_admin = self.ctx.login_custom(
                AccountConfig(
                    name=tenant_admin_alias,
                    login_field="account",
                    principal=tenant_phone,
                    password=tenant_phone,
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                ),
                alias=tenant_admin_alias,
            )

            company_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/org/node",
                session_state=tenant_admin,
                json_body={
                    "nodeName": f"授权公司{suffix[-4:]}",
                    "shortName": f"公{suffix[-4:]}",
                    "memo": "company for tenant app grant",
                    "parentId": tenant_root_org_id,
                    "nodeCategory": 1,
                    "existType": 0,
                    "nodeType": 0,
                },
            )
            ensure_http_status(company_response, 200)
            company = ensure_api_status(company_response.json())
            company_id = company["id"]

            department_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/org/node",
                session_state=tenant_admin,
                json_body={
                    "nodeName": f"授权部门{suffix[-4:]}",
                    "shortName": f"部{suffix[-4:]}",
                    "memo": "department for tenant app grant",
                    "parentId": company_id,
                    "nodeCategory": 1,
                    "existType": 0,
                    "nodeType": 1,
                },
            )
            ensure_http_status(department_response, 200)
            department = ensure_api_status(department_response.json())
            department_id = department["id"]

            temp_user_account = f"tenant_app_{suffix[-6:]}"
            temp_user_phone = self._build_phone("138", suffix)
            create_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                json_body={
                    "nodeId": department_id,
                    "account": temp_user_account,
                    "phone": temp_user_phone,
                    "realName": "应用授权用户",
                    "email": f"{temp_user_account}@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_http_status(create_user_response, 200)
            temp_user_id = ensure_api_status(create_user_response.json())
            self.assertTrue(temp_user_id, "临时授权用户创建失败")

            temp_user = self.ctx.login_custom(
                AccountConfig(
                    name=temp_user_alias,
                    login_field="account",
                    principal=temp_user_account,
                    password=temp_user_phone,
                    tenant_id=tenant_id,
                    org_ids=[company_id],
                    extra_headers={},
                ),
                alias=temp_user_alias,
            )

            target_perm = self._pick_portal_perm(tenant_admin)
            self.assertFalse(
                self._check_perm(temp_user, target_perm["code"]),
                "授权前临时用户不应具备目标权限",
            )

            create_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=tenant_admin,
                json_body={
                    "nodeName": f"租户应用权限组{suffix[-4:]}",
                    "memo": "tenant app grant perm group",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_http_status(create_group_response, 200)
            perm_group = ensure_api_status(create_group_response.json())
            perm_group_id = perm_group["id"]

            create_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=tenant_admin,
                json_body={
                    "nameCn": f"租户应用权限单元{suffix[-4:]}",
                    "memo": "tenant app grant perm unit",
                    "belongTo": perm_group_id,
                },
            )
            ensure_http_status(create_unit_response, 200)
            perm_unit = ensure_api_status(create_unit_response.json())
            perm_unit_id = perm_unit["id"]

            pack_perm_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/resources",
                session_state=tenant_admin,
                json_body={
                    "unitId": perm_unit_id,
                    "appId": "portal",
                    "permIds": [target_perm["id"]],
                },
            )
            ensure_http_status(pack_perm_response, 200)
            ensure_api_status(pack_perm_response.json())

            pack_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/users",
                session_state=tenant_admin,
                json_body={
                    "unitIdList": [perm_unit_id],
                    "userList": [{"userId": temp_user_id, "orgId": company_id}],
                },
            )
            ensure_http_status(pack_user_response, 200)
            ensure_api_status(pack_user_response.json())

            list_grant_user_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit/users",
                session_state=tenant_admin,
                params={"page": 1, "size": 20, "unitId": perm_unit_id},
            )
            ensure_http_status(list_grant_user_response, 200)
            grant_user_page = ensure_api_status(list_grant_user_response.json())
            perm_grant_ids = [
                item["grantId"] for item in grant_user_page["list"] if item["userId"] == temp_user_id
            ]
            self.assertTrue(perm_grant_ids, "权限单元授权用户列表未找到临时用户")

            self.ctx.logout(temp_user_alias)
            temp_user = self.ctx.relogin(temp_user_alias)
            self.assertTrue(
                self._check_perm(temp_user, target_perm["code"]),
                "租户授权应用并授予角色后，重新登录仍未获得权限",
            )

            delete_grant_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app/delete",
                session_state=super_admin_session,
                params={"id": tenant_app_grant_id},
            )
            ensure_http_status(delete_grant_response, 200)
            ensure_api_status(delete_grant_response.json())
            tenant_app_grant_id = ""

            self.ctx.logout(temp_user_alias)
            temp_user = self.ctx.relogin(temp_user_alias)
            self.assertFalse(
                self._check_perm(temp_user, target_perm["code"]),
                "撤销租户应用授权后，重新登录仍保留权限",
            )
        finally:
            if perm_grant_ids and tenant_admin is not None:
                revoke_user_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/unit/users/delete-batch",
                    session_state=tenant_admin,
                    json_body={"ids": perm_grant_ids},
                )
                ensure_http_status(revoke_user_response, 200)
                ensure_api_status(revoke_user_response.json())

            if perm_unit_id and tenant_admin is not None:
                disable_unit_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/unit/forbidden",
                    session_state=tenant_admin,
                    json_body={"unitId": perm_unit_id, "forbidden": 1},
                )
                ensure_http_status(disable_unit_response, 200)
                ensure_api_status(disable_unit_response.json())

                delete_unit_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/unit/delete",
                    session_state=tenant_admin,
                    params={"id": perm_unit_id},
                )
                ensure_http_status(delete_unit_response, 200)
                ensure_api_status(delete_unit_response.json())

            if perm_group_id and tenant_admin is not None:
                delete_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group/delete",
                    session_state=tenant_admin,
                    params={"id": perm_group_id},
                )
                ensure_http_status(delete_group_response, 200)
                ensure_api_status(delete_group_response.json())

            if temp_user_id and tenant_admin is not None:
                delete_user_safely(self.ctx, tenant_admin, temp_user_id)

            if department_id and tenant_admin is not None:
                delete_department_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/org/node/delete",
                    session_state=tenant_admin,
                    params={"id": department_id},
                )
                ensure_http_status(delete_department_response, 200)
                ensure_api_status(delete_department_response.json())

            if company_id and tenant_admin is not None:
                delete_company_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/org/node/delete",
                    session_state=tenant_admin,
                    params={"id": company_id},
                )
                ensure_http_status(delete_company_response, 200)
                ensure_api_status(delete_company_response.json())

            if tenant_admin is not None and tenant_root_org_id:
                delete_org_tree_safely(self.ctx, tenant_admin, tenant_root_org_id)

            if tenant_app_grant_id:
                delete_grant_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/grant/app/delete",
                    session_state=super_admin_session,
                    params={"id": tenant_app_grant_id},
                )
                ensure_http_status(delete_grant_response, 200)
                ensure_api_status(delete_grant_response.json())

            if tenant_id:
                delete_tenant_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/tenant/delete",
                    session_state=super_admin_session,
                    params={"id": tenant_id},
                )
                ensure_http_status(delete_tenant_response, 200)
                ensure_api_status(delete_tenant_response.json())

    def _pick_portal_perm(self, session_state) -> dict[str, str]:
        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/resource/tree",
            session_state=session_state,
            params={"appId": "portal", "withPerm": "true", "withApi": "true"},
        )
        ensure_http_status(tree_response, 200)
        tree = ensure_api_status(tree_response.json())
        perms: list[dict[str, str]] = []
        self._walk_perm_tree(tree, perms)
        self.assertTrue(perms, "portal 应用下未找到可授权权限点")
        return perms[0]

    def _walk_perm_tree(self, nodes: list[dict], perms: list[dict[str, str]]) -> None:
        for node in nodes:
            data = node.get("data") or {}
            perm = data.get("perm") or {}
            if perm.get("id") and perm.get("permCode"):
                perms.append({"id": perm["id"], "code": perm["permCode"]})
            self._walk_perm_tree(node.get("children") or [], perms)

    def _check_perm(self, session_state, perm_code: str) -> bool:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/client/perm-check",
            session_state=session_state,
            params={"permCode": perm_code},
        )
        ensure_http_status(response, 200)
        return bool(ensure_api_status(response.json()))

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
