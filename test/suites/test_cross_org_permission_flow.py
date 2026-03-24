from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_perm_unit_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class CrossOrgPermissionFlowTestCase(BaseFlowTestCase):
    def test_same_tenant_wrong_org_context_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("134", suffix)

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_a_id = ""
        company_b_id = ""
        dept_a_id = ""
        dept_b_id = ""
        target_user_id = ""
        scoped_user_id = ""
        perm_group_id = ""
        perm_unit_id = ""
        tenant_admin = None
        scoped_user_alias = f"cross_org_scope_user_{suffix}"

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"跨组织租户{suffix[-6:]}",
                "shortName": f"跨组{suffix[-4:]}",
                "logo": "",
                "memo": "cross org permission flow tenant",
                "contactUser": "跨组织管理员",
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
                    name=f"cross_org_admin_{tenant_id}",
                    login_field="account",
                    principal=tenant_phone,
                    password=tenant_phone,
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                )
            )

            company_a_id = self._create_org(tenant_admin, tenant_root_org_id, f"跨组织公司A{suffix[-4:]}", f"跨A{suffix[-4:]}", 0)
            dept_a_id = self._create_org(tenant_admin, company_a_id, f"跨组织部门A{suffix[-4:]}", f"跨部A{suffix[-4:]}", 1)
            company_b_id = self._create_org(tenant_admin, tenant_root_org_id, f"跨组织公司B{suffix[-4:]}", f"跨B{suffix[-4:]}", 0)
            dept_b_id = self._create_org(tenant_admin, company_b_id, f"跨组织部门B{suffix[-4:]}", f"跨部B{suffix[-4:]}", 1)

            target_user_id = self._create_user(
                tenant_admin,
                dept_a_id,
                f"cross_org_target_{suffix[-6:]}",
                self._build_phone("179", suffix),
                "跨组织目标用户",
            )

            scoped_user_account = f"cross_org_scope_{suffix[-6:]}"
            scoped_user_phone = self._build_phone("178", suffix)
            scoped_user_id = self._create_user(
                tenant_admin,
                dept_a_id,
                scoped_user_account,
                scoped_user_phone,
                "跨组织上下文用户",
            )

            add_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_admin,
                json_body={"userId": scoped_user_id, "nodeIds": [dept_b_id]},
            )
            ensure_http_status(add_org_response, 200)
            ensure_api_status(add_org_response.json())

            scoped_user = self.ctx.login_custom(
                AccountConfig(
                    name=scoped_user_alias,
                    login_field="account",
                    principal=scoped_user_account,
                    password=scoped_user_phone,
                    tenant_id=tenant_id,
                    org_ids=[company_a_id, company_b_id],
                    extra_headers={},
                ),
                alias=scoped_user_alias,
            )

            wrong_org_query_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user",
                session_state=scoped_user,
                org_id=company_a_id,
                params={"page": 1, "size": 20, "nodeId": company_b_id, "level": 2},
            )
            ensure_api_client_error(wrong_org_query_response, 11)

            create_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=tenant_admin,
                json_body={
                    "nodeName": f"跨组织权限组{suffix[-4:]}",
                    "memo": "cross org perm group",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_http_status(create_group_response, 200)
            created_group = ensure_api_status(create_group_response.json())
            perm_group_id = created_group["id"]

            create_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=tenant_admin,
                json_body={
                    "nameCn": f"跨组织权限单元{suffix[-4:]}",
                    "memo": "cross org perm unit",
                    "belongTo": perm_group_id,
                },
            )
            ensure_http_status(create_unit_response, 200)
            created_unit = ensure_api_status(create_unit_response.json())
            perm_unit_id = created_unit["id"]

            wrong_org_pack_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/users",
                session_state=tenant_admin,
                json_body={
                    "unitIdList": [perm_unit_id],
                    "userList": [{"userId": target_user_id, "orgId": company_b_id}],
                },
            )
            ensure_api_client_error(wrong_org_pack_user_response, 10)

            wrong_org_user_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                json_body={
                    "unitIdList": [perm_unit_id],
                    "userList": [{"userId": target_user_id, "orgId": company_b_id}],
                },
            )
            ensure_api_client_error(wrong_org_user_unit_response, 10)
        finally:
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

            if scoped_user_id and tenant_admin is not None:
                delete_user_safely(self.ctx, tenant_admin, scoped_user_id)
            if target_user_id and tenant_admin is not None:
                delete_user_safely(self.ctx, tenant_admin, target_user_id)

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
                "memo": "cross org permission flow org",
                "parentId": parent_id,
                "nodeCategory": 1,
                "existType": 0,
                "nodeType": node_type,
            },
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())["id"]

    def _create_user(self, session_state, node_id: str, account: str, phone: str, real_name: str) -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user",
            session_state=session_state,
            json_body={
                "nodeId": node_id,
                "account": account,
                "phone": phone,
                "realName": real_name,
                "email": f"{account}@example.com",
                "avatar": "",
                "employeeType": 0,
            },
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
