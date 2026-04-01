from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_perm_unit_safely, delete_user_safely
from common.resource_match import collect_portal_perms_for_endpoints
from suites.base import BaseFlowTestCase


class GrantedOrgScopeFlowTestCase(BaseFlowTestCase):
    def test_granted_endpoints_change_with_org_scope(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("135", suffix)

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_a_id = ""
        company_b_id = ""
        dept_a_id = ""
        dept_b_id = ""
        user_id = ""
        perm_group_id = ""
        perm_unit_id = ""
        perm_grant_ids: list[str] = []
        tenant_admin = None
        scoped_user_alias = f"granted_scope_user_{suffix}"

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"授权范围租户{suffix[-6:]}",
                "shortName": f"范围{suffix[-4:]}",
                "logo": "",
                "memo": "granted org scope flow tenant",
                "contactUser": "范围管理员",
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
                json_body={"tenantId": tenant_id, "appId": "portal", "grantAll": True, "permIds": []},
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
                    name=f"granted_scope_admin_{tenant_id}",
                    login_field="account",
                    principal=tenant_phone,
                    password=tenant_phone,
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                )
            )

            company_a_id = self._create_org(tenant_admin, tenant_root_org_id, f"范围公司A{suffix[-4:]}", f"范A{suffix[-4:]}", 0)
            dept_a_id = self._create_org(tenant_admin, company_a_id, f"范围部门A{suffix[-4:]}", f"范部A{suffix[-4:]}", 1)
            company_b_id = self._create_org(tenant_admin, tenant_root_org_id, f"范围公司B{suffix[-4:]}", f"范B{suffix[-4:]}", 0)
            dept_b_id = self._create_org(tenant_admin, company_b_id, f"范围部门B{suffix[-4:]}", f"范部B{suffix[-4:]}", 1)

            user_account = f"granted_scope_{suffix[-6:]}"
            user_phone = self._build_phone("179", suffix)
            create_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                json_body={
                    "nodeId": dept_a_id,
                    "account": user_account,
                    "phone": user_phone,
                    "realName": "授权范围用户",
                    "email": f"{user_account}@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_http_status(create_user_response, 200)
            user_id = ensure_api_status(create_user_response.json())

            add_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_admin,
                json_body={"userId": user_id, "nodeIds": [dept_b_id]},
            )
            ensure_http_status(add_org_response, 200)
            ensure_api_status(add_org_response.json())

            scoped_user = self.ctx.login_custom(
                AccountConfig(
                    name=scoped_user_alias,
                    login_field="account",
                    principal=user_account,
                    password=user_phone,
                    tenant_id=tenant_id,
                    org_ids=[company_a_id, company_b_id],
                    extra_headers={},
                ),
                alias=scoped_user_alias,
            )

            endpoint_perms = collect_portal_perms_for_endpoints(
                self.ctx,
                tenant_admin,
                [
                    ("GET", "/api/portal/v1/granted/app"),
                    ("GET", "/api/portal/v1/granted/app/res-tree"),
                ],
            )

            before_granted_a_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=scoped_user,
                org_id=company_a_id,
            )
            ensure_http_status(before_granted_a_response, 200)
            before_granted_a = ensure_api_status(before_granted_a_response.json())
            self.assertEqual(before_granted_a, [], "授权前当前组织下不应返回任何应用")

            if endpoint_perms:
                create_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group",
                    session_state=tenant_admin,
                    json_body={
                        "nodeName": f"范围权限组{suffix[-4:]}",
                        "memo": "granted org scope perm group",
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
                        "nameCn": f"范围权限单元{suffix[-4:]}",
                        "memo": "granted org scope perm unit",
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
                        "permIds": [item["id"] for item in endpoint_perms],
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
                        "userList": [{"userId": user_id, "orgId": company_a_id}],
                    },
                )
                ensure_http_status(pack_user_response, 200)
                ensure_api_status(pack_user_response.json())

                grant_user_list_response = self.ctx.client.request(
                    "GET",
                    "/api/portal/v1/perm/unit/users",
                    session_state=tenant_admin,
                    params={"page": 1, "size": 20, "unitId": perm_unit_id},
                )
                ensure_http_status(grant_user_list_response, 200)
                grant_user_page = ensure_api_status(grant_user_list_response.json())
                perm_grant_ids = [item["grantId"] for item in grant_user_page["list"] if item["userId"] == user_id]
                self.assertTrue(perm_grant_ids, "按组织授权后未查到目标用户")

                self.ctx.logout(scoped_user_alias)
                scoped_user = self.ctx.relogin(scoped_user_alias)

            granted_a_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=scoped_user,
                org_id=company_a_id,
            )
            ensure_http_status(granted_a_response, 200)
            granted_a = ensure_api_status(granted_a_response.json())
            self.assertTrue(any(item["appId"] == "portal" for item in granted_a))

            granted_b_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=scoped_user,
                org_id=company_b_id,
            )
            ensure_http_status(granted_b_response, 200)
            granted_b = ensure_api_status(granted_b_response.json())
            self.assertEqual(granted_b, [], "未授权组织下不应返回任何应用")

            res_tree_a_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app/res-tree",
                session_state=scoped_user,
                org_id=company_a_id,
                params={"appId": "portal"},
            )
            ensure_http_status(res_tree_a_response, 200)
            res_tree_a = ensure_api_status(res_tree_a_response.json())
            if endpoint_perms:
                self.assertTrue(res_tree_a, "有权限组织下资源树不应为空")

            res_tree_b_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app/res-tree",
                session_state=scoped_user,
                org_id=company_b_id,
                params={"appId": "portal"},
            )
            if endpoint_perms:
                ensure_api_client_error(res_tree_b_response, 11)
            else:
                ensure_http_status(res_tree_b_response, 200)
                ensure_api_status(res_tree_b_response.json())
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
                delete_perm_unit_safely(self.ctx, tenant_admin, perm_unit_id)

            if perm_group_id and tenant_admin is not None:
                delete_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group/delete",
                    session_state=tenant_admin,
                    params={"id": perm_group_id},
                )
                ensure_http_status(delete_group_response, 200)
                ensure_api_status(delete_group_response.json())

            if user_id and tenant_admin is not None:
                delete_user_safely(self.ctx, tenant_admin, user_id)

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

    def _create_org(self, session_state, parent_id: str, node_name: str, short_name: str, node_type: int) -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=session_state,
            json_body={
                "nodeName": node_name,
                "shortName": short_name,
                "memo": "granted org scope flow org",
                "parentId": parent_id,
                "nodeCategory": 1,
                "existType": 0,
                "nodeType": node_type,
            },
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())["id"]

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
