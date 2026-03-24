from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class UserExtraFlowTestCase(BaseFlowTestCase):
    def test_user_select_delete_batch_and_admin_reset_passwd_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("136", suffix)

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_id = ""
        department_id = ""
        user_ids: list[str] = []
        tenant_admin = None

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"用户扩展租户{suffix[-6:]}",
                "shortName": f"扩展{suffix[-4:]}",
                "logo": "",
                "memo": "user extra flow tenant",
                "contactUser": "扩展管理员",
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
                    name=f"user_extra_admin_{tenant_id}",
                    login_field="account",
                    principal=tenant_phone,
                    password=tenant_phone,
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                )
            )

            company_id = self._create_org(
                tenant_admin,
                tenant_root_org_id,
                f"扩展公司{suffix[-4:]}",
                f"扩公{suffix[-4:]}",
                0,
            )
            department_id = self._create_org(
                tenant_admin,
                company_id,
                f"扩展部门{suffix[-4:]}",
                f"扩部{suffix[-4:]}",
                1,
            )

            default_select_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/for-select",
                session_state=tenant_admin,
                params={"level": 2},
            )
            ensure_http_status(default_select_response, 200)
            default_selected = ensure_api_status(default_select_response.json())
            self.assertGreaterEqual(default_selected["total"], 1, "for-select 缺省 nodeId 时应按默认上下文返回结果")

            created_users: list[dict[str, str]] = []
            for index in range(2):
                account = f"user_extra_{suffix[-6:]}_{index}"
                phone = self._build_phone(f"17{9-index}", suffix + str(index))
                create_user_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/user",
                    session_state=tenant_admin,
                    json_body={
                        "nodeId": department_id,
                        "account": account,
                        "phone": phone,
                        "realName": f"扩展用户{index}",
                        "email": f"{account}@example.com",
                        "avatar": "",
                        "employeeType": 0,
                    },
                )
                ensure_http_status(create_user_response, 200)
                user_id = ensure_api_status(create_user_response.json())
                user_ids.append(user_id)
                created_users.append({"id": user_id, "account": account, "phone": phone})

            select_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/for-select",
                session_state=tenant_admin,
                params={"nodeId": company_id, "level": 2, "keyword": created_users[0]["account"]},
            )
            ensure_http_status(select_response, 200)
            selected = ensure_api_status(select_response.json())
            selected_items = selected.get("list") or []
            self.assertTrue(
                any(item["id"] == created_users[0]["id"] for item in selected_items),
                "for-select 未查到目标用户",
            )

            weak_pass_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/admin-reset-passwd",
                session_state=tenant_admin,
                json_body={"userId": created_users[0]["id"], "password": "123456"},
            )
            ensure_api_client_error(weak_pass_response, 10)

            new_password = f"Abc!{suffix[-6:]}"
            reset_pass_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/admin-reset-passwd",
                session_state=tenant_admin,
                json_body={"userId": created_users[0]["id"], "password": new_password},
            )
            ensure_http_status(reset_pass_response, 200)
            ensure_api_status(reset_pass_response.json())

            reset_login = self.ctx.login_custom(
                AccountConfig(
                    name=f"user_extra_reset_{suffix}",
                    login_field="account",
                    principal=created_users[0]["account"],
                    password=new_password,
                    tenant_id=tenant_id,
                    org_ids=[company_id],
                    extra_headers={},
                ),
                alias=f"user_extra_reset_{suffix}",
            )
            self.assertEqual(reset_login.user_id, created_users[0]["id"])

            invalid_delete_batch_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/delete-batch",
                session_state=tenant_admin,
                json_body={"ids": []},
            )
            ensure_api_client_error(invalid_delete_batch_response)

            delete_batch_blocked_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/delete-batch",
                session_state=tenant_admin,
                json_body={"ids": [item["id"] for item in created_users]},
            )
            ensure_api_client_error(delete_batch_blocked_response, 10)

            for item in created_users:
                forbid_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/user/forbidden",
                    session_state=tenant_admin,
                    json_body={"userId": item["id"], "forbidden": 1},
                )
                ensure_http_status(forbid_response, 200)
                ensure_api_status(forbid_response.json())

            delete_batch_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/delete-batch",
                session_state=tenant_admin,
                json_body={"ids": [item["id"] for item in created_users]},
            )
            ensure_http_status(delete_batch_response, 200)
            ensure_api_status(delete_batch_response.json())
            user_ids.clear()

            list_after_delete_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                params={"page": 1, "size": 20, "keyword": "user_extra_" + suffix[-6:]},
            )
            ensure_http_status(list_after_delete_response, 200)
            list_after_delete = ensure_api_status(list_after_delete_response.json())
            remaining_users = list_after_delete.get("list") or []
            self.assertFalse(
                any(item["id"] in {u["id"] for u in created_users} for item in remaining_users),
                "批量删除后，用户列表中仍存在已删除用户",
            )
        finally:
            for user_id in list(user_ids):
                if tenant_admin is not None:
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
                "memo": "user extra flow org",
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
