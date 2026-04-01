from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_perm_unit_safely, delete_user_safely
from common.resource_match import collect_portal_perms_for_endpoints
from suites.base import BaseFlowTestCase


class GrantedResourceFlowTestCase(BaseFlowTestCase):
    def test_granted_app_and_resource_query_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("139", suffix)

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_id = ""
        department_id = ""
        user_id = ""
        perm_group_id = ""
        perm_unit_id = ""
        perm_grant_ids: list[str] = []
        tenant_admin_alias = f"granted_tenant_admin_{suffix}"
        temp_user_alias = f"granted_temp_user_{suffix}"
        tenant_admin = None

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"已授权资源租户{suffix[-6:]}",
                "shortName": f"授权查{suffix[-4:]}",
                "logo": "",
                "memo": "granted resource flow tenant",
                "contactUser": "授权管理员",
                "contactPhone": tenant_phone,
            },
        )
        ensure_http_status(create_tenant_response, 200)
        tenant = ensure_api_status(create_tenant_response.json())
        tenant_id = tenant["id"]
        tenant_root_org_id = f"{tenant_id}-ORG-000001"

        try:
            missing_tree_param_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/grant/app/tree",
                session_state=super_admin_session,
                params={"appId": "portal"},
            )
            ensure_api_client_error(missing_tree_param_response)

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

            grant_tree_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/grant/app/tree",
                session_state=super_admin_session,
                params={"tenantId": tenant_id, "appId": "portal"},
            )
            ensure_http_status(grant_tree_response, 200)
            grant_tree = ensure_api_status(grant_tree_response.json())
            self.assertTrue(grant_tree, "租户授权 portal 后，授权树不应为空")

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

            company_id = self._create_org(
                tenant_admin,
                tenant_root_org_id,
                f"资源公司{suffix[-4:]}",
                f"资公{suffix[-4:]}",
                node_type=0,
            )
            department_id = self._create_org(
                tenant_admin,
                company_id,
                f"资源部门{suffix[-4:]}",
                f"资部{suffix[-4:]}",
                node_type=1,
            )

            user_account = f"granted_user_{suffix[-6:]}"
            user_phone = self._build_phone("179", suffix)
            create_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                json_body={
                    "nodeId": department_id,
                    "account": user_account,
                    "phone": user_phone,
                    "realName": "授权资源用户",
                    "email": f"{user_account}@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_http_status(create_user_response, 200)
            user_id = ensure_api_status(create_user_response.json())

            temp_user = self.ctx.login_custom(
                AccountConfig(
                    name=temp_user_alias,
                    login_field="account",
                    principal=user_account,
                    password=user_phone,
                    tenant_id=tenant_id,
                    org_ids=[company_id],
                    extra_headers={},
                ),
                alias=temp_user_alias,
            )

            target_perms = collect_portal_perms_for_endpoints(
                self.ctx,
                tenant_admin,
                [
                    ("GET", "/api/portal/v1/granted/app"),
                    ("GET", "/api/portal/v1/granted/app/res-tree"),
                ],
            )

            before_granted_app_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=temp_user,
            )
            ensure_http_status(before_granted_app_response, 200)
            before_granted_apps = ensure_api_status(before_granted_app_response.json())
            self.assertEqual(before_granted_apps, [], "授权前普通用户不应看到任何已授权应用")

            if target_perms:
                create_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group",
                    session_state=tenant_admin,
                    json_body={
                        "nodeName": f"授权资源分组{suffix[-4:]}",
                        "memo": "granted resource perm group",
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
                        "nameCn": f"授权资源单元{suffix[-4:]}",
                        "memo": "granted resource perm unit",
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
                        "permIds": [item["id"] for item in target_perms],
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
                        "userList": [{"userId": user_id, "orgId": company_id}],
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
                self.assertTrue(perm_grant_ids, "授权资源用户列表未查到临时用户")

                self.ctx.logout(temp_user_alias)
                temp_user = self.ctx.relogin(temp_user_alias)

            granted_app_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=temp_user,
            )
            ensure_http_status(granted_app_response, 200)
            granted_apps = ensure_api_status(granted_app_response.json())
            self.assertTrue(
                any(item["appId"] == "portal" for item in granted_apps),
                "授权后普通用户应在已授权应用列表中看到 portal",
            )

            missing_app_id_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app/res-tree",
                session_state=temp_user,
            )
            ensure_api_client_error(missing_app_id_response)

            granted_tree_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app/res-tree",
                session_state=temp_user,
                params={"appId": "portal"},
            )
            ensure_http_status(granted_tree_response, 200)
            granted_tree = ensure_api_status(granted_tree_response.json())
            if target_perms:
                self.assertTrue(granted_tree, "授权后普通用户查询 portal 资源树不应为空")

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

            granted_app_after_revoke_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/granted/app",
                session_state=temp_user,
            )
            ensure_http_status(granted_app_after_revoke_response, 200)
            granted_apps_after_revoke = ensure_api_status(granted_app_after_revoke_response.json())
            self.assertFalse(
                any(item["appId"] == "portal" for item in granted_apps_after_revoke),
                "撤销租户应用授权后，portal 不应继续出现在应用列表中",
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

    def _create_org(
        self,
        session_state,
        parent_id: str,
        node_name: str,
        short_name: str,
        node_type: int,
    ) -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=session_state,
            json_body={
                "nodeName": node_name,
                "shortName": short_name,
                "memo": "granted resource flow node",
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
