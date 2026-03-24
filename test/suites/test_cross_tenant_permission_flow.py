from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_perm_unit_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class CrossTenantPermissionFlowTestCase(BaseFlowTestCase):
    def test_cross_tenant_and_cross_org_permission_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))

        tenant_a = self._create_tenant_bundle(super_admin_session, suffix, "A", "137")
        tenant_b = self._create_tenant_bundle(super_admin_session, suffix, "B", "138")

        try:
            company_a_id = self._create_org(
                tenant_a["admin_session"],
                tenant_a["root_org_id"],
                f"越权公司A{suffix[-4:]}",
                f"越A{suffix[-4:]}",
                0,
            )
            dept_a_id = self._create_org(
                tenant_a["admin_session"],
                company_a_id,
                f"越权部门A{suffix[-4:]}",
                f"越部A{suffix[-4:]}",
                1,
            )
            company_b_id = self._create_org(
                tenant_b["admin_session"],
                tenant_b["root_org_id"],
                f"越权公司B{suffix[-4:]}",
                f"越B{suffix[-4:]}",
                0,
            )
            dept_b_id = self._create_org(
                tenant_b["admin_session"],
                company_b_id,
                f"越权部门B{suffix[-4:]}",
                f"越部B{suffix[-4:]}",
                1,
            )

            user_a_id = self._create_user(
                tenant_a["admin_session"],
                dept_a_id,
                f"cross_a_{suffix[-6:]}",
                self._build_phone("179", suffix),
                "跨租户用户A",
            )
            tenant_a["user_ids"].append(user_a_id)

            user_b_id = self._create_user(
                tenant_b["admin_session"],
                dept_b_id,
                f"cross_b_{suffix[-6:]}",
                self._build_phone("178", suffix),
                "跨租户用户B",
            )
            tenant_b["user_ids"].append(user_b_id)

            cross_tenant_user_detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_b_id}",
                session_state=tenant_a["admin_session"],
            )
            ensure_api_client_error(cross_tenant_user_detail_response, 11)

            cross_tenant_add_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_a["admin_session"],
                json_body={"userId": user_a_id, "nodeIds": [dept_b_id]},
            )
            ensure_api_client_error(cross_tenant_add_org_response, 10)

            cross_tenant_switch_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/switch-main",
                session_state=tenant_a["admin_session"],
                json_body={"userId": user_b_id, "orgId": company_b_id},
            )
            ensure_api_client_error(cross_tenant_switch_org_response, 11)

            create_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=tenant_a["admin_session"],
                json_body={
                    "nodeName": f"跨租户权限组{suffix[-4:]}",
                    "memo": "cross tenant perm group",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_http_status(create_group_response, 200)
            created_group = ensure_api_status(create_group_response.json())
            tenant_a["perm_group_id"] = created_group["id"]

            create_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=tenant_a["admin_session"],
                json_body={
                    "nameCn": f"跨租户权限单元{suffix[-4:]}",
                    "memo": "cross tenant perm unit",
                    "belongTo": tenant_a["perm_group_id"],
                },
            )
            ensure_http_status(create_unit_response, 200)
            created_unit = ensure_api_status(create_unit_response.json())
            tenant_a["perm_unit_id"] = created_unit["id"]

            cross_tenant_pack_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/users",
                session_state=tenant_a["admin_session"],
                json_body={
                    "unitIdList": [tenant_a["perm_unit_id"]],
                    "userList": [{"userId": user_b_id, "orgId": company_b_id}],
                },
            )
            ensure_api_client_error(cross_tenant_pack_user_response, 10)
        finally:
            self._cleanup_tenant_bundle(super_admin_session, tenant_a)
            self._cleanup_tenant_bundle(super_admin_session, tenant_b)

    def _create_tenant_bundle(self, super_admin_session, seed: str, label: str, phone_prefix: str) -> dict:
        tenant_phone = self._build_phone(phone_prefix, seed)
        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"跨租户{label}{seed[-6:]}",
                "shortName": f"跨{label}{seed[-4:]}",
                "logo": "",
                "memo": f"cross tenant bundle {label}",
                "contactUser": f"租户{label}管理员",
                "contactPhone": tenant_phone,
            },
        )
        ensure_http_status(create_tenant_response, 200)
        tenant = ensure_api_status(create_tenant_response.json())
        tenant_id = tenant["id"]
        root_org_id = f"{tenant_id}-ORG-000001"

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

        admin_session = self.ctx.login_custom(
            AccountConfig(
                name=f"cross_tenant_admin_{tenant_id}",
                login_field="account",
                principal=tenant_phone,
                password=tenant_phone,
                tenant_id=tenant_id,
                org_ids=[root_org_id],
                extra_headers={},
            )
        )
        return {
            "tenant_id": tenant_id,
            "root_org_id": root_org_id,
            "app_grant_id": portal_grant["grantId"],
            "admin_session": admin_session,
            "user_ids": [],
            "perm_group_id": "",
            "perm_unit_id": "",
        }

    def _cleanup_tenant_bundle(self, super_admin_session, bundle: dict) -> None:
        admin_session = bundle.get("admin_session")
        if bundle.get("perm_unit_id") and admin_session is not None:
            delete_perm_unit_safely(self.ctx, admin_session, bundle["perm_unit_id"])

        if bundle.get("perm_group_id") and admin_session is not None:
            delete_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group/delete",
                session_state=admin_session,
                params={"id": bundle["perm_group_id"]},
            )
            ensure_http_status(delete_group_response, 200)
            ensure_api_status(delete_group_response.json())

        if admin_session is not None:
            for user_id in list(bundle.get("user_ids") or []):
                delete_user_safely(self.ctx, admin_session, user_id)

        if admin_session is not None and bundle.get("root_org_id"):
            delete_org_tree_safely(self.ctx, admin_session, bundle["root_org_id"])

        if bundle.get("app_grant_id"):
            delete_grant_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app/delete",
                session_state=super_admin_session,
                params={"id": bundle["app_grant_id"]},
            )
            ensure_http_status(delete_grant_response, 200)
            ensure_api_status(delete_grant_response.json())

        if bundle.get("tenant_id"):
            delete_tenant_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/tenant/delete",
                session_state=super_admin_session,
                params={"id": bundle["tenant_id"]},
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
                "memo": "cross tenant permission flow org",
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
