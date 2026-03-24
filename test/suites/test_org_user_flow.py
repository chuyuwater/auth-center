from __future__ import annotations

import time

from common.assertions import ensure_api_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class OrgUserFlowTestCase(BaseFlowTestCase):
    def test_org_and_user_management_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_name = f"组织用户租户{suffix[-6:]}"
        tenant_phone = self._build_phone("137", suffix)

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": tenant_name,
                "shortName": f"租户{suffix[-4:]}",
                "logo": "",
                "memo": "org user flow tenant",
                "contactUser": "租户管理员",
                "contactPhone": tenant_phone,
            },
        )
        ensure_http_status(create_tenant_response, 200)
        tenant = ensure_api_status(create_tenant_response.json())
        tenant_id = tenant["id"]
        tenant_root_org_id = f"{tenant_id}-ORG-000001"
        tenant_app_grant_id = ""

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
                name=f"tenant_admin_{tenant_id}",
                login_field="account",
                principal=tenant_phone,
                password=tenant_phone,
                tenant_id=tenant_id,
                org_ids=[tenant_root_org_id],
                extra_headers={},
            )
        )

        company_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=tenant_admin,
            json_body={
                "nodeName": f"自动化公司{suffix[-4:]}",
                "shortName": f"公司{suffix[-4:]}",
                "memo": "company for e2e",
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
                "nodeName": f"自动化部门{suffix[-4:]}",
                "shortName": f"部门{suffix[-4:]}",
                "memo": "department for e2e",
                "parentId": company_id,
                "nodeCategory": 1,
                "existType": 0,
                "nodeType": 1,
            },
        )
        ensure_http_status(department_response, 200)
        department = ensure_api_status(department_response.json())
        department_id = department["id"]

        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/org/tree",
            session_state=tenant_admin,
        )
        ensure_http_status(tree_response, 200)
        tree = ensure_api_status(tree_response.json())
        self.assertIsNotNone(self._find_org_node(tree, company_id), "组织树中未找到新建公司")
        self.assertIsNotNone(self._find_org_node(tree, department_id), "组织树中未找到新建部门")

        company_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/org/node/{company_id}",
            session_state=tenant_admin,
        )
        ensure_http_status(company_detail_response, 200)
        company_detail = ensure_api_status(company_detail_response.json())
        self.assertEqual(company_detail["id"], company_id)

        update_company_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/org/node/{company_id}",
            session_state=tenant_admin,
            json_body={
                "nodeName": f"自动化公司{suffix[-4:]}-更新",
                "shortName": f"公更{suffix[-4:]}",
                "memo": "company updated by e2e",
                "parentId": tenant_root_org_id,
                "nodeCategory": 1,
                "existType": 0,
            },
        )
        ensure_http_status(update_company_response, 200)
        updated_company = ensure_api_status(update_company_response.json())
        self.assertEqual(updated_company["nodeName"], f"自动化公司{suffix[-4:]}-更新")

        disable_department_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/status",
            session_state=tenant_admin,
            json_body={"nodeId": department_id, "forbidden": 1},
        )
        ensure_http_status(disable_department_response, 200)
        ensure_api_status(disable_department_response.json())

        enable_department_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/status",
            session_state=tenant_admin,
            json_body={"nodeId": department_id, "forbidden": 0},
        )
        ensure_http_status(enable_department_response, 200)
        ensure_api_status(enable_department_response.json())

        user_account = f"auto_user_{suffix[-6:]}"
        user_phone = self._build_phone("136", suffix)
        user_email = f"{user_account}@example.com"
        create_user_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user",
            session_state=tenant_admin,
            json_body={
                "nodeId": department_id,
                "account": user_account,
                "phone": user_phone,
                "realName": "自动化用户",
                "email": user_email,
                "avatar": "",
                "employeeType": 0,
            },
        )
        ensure_http_status(create_user_response, 200)
        user_id = ensure_api_status(create_user_response.json())
        self.assertTrue(user_id, "创建用户未返回 userId")

        list_user_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/user",
            session_state=tenant_admin,
            params={"page": 1, "size": 20, "keyword": user_account},
        )
        ensure_http_status(list_user_response, 200)
        listed_users = ensure_api_status(list_user_response.json())
        self.assertTrue(any(item["id"] == user_id for item in listed_users["list"]), "用户列表未查到新建用户")

        user_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/user/{user_id}",
            session_state=tenant_admin,
        )
        ensure_http_status(user_detail_response, 200)
        user_detail = ensure_api_status(user_detail_response.json())
        self.assertEqual(user_detail["id"], user_id)
        self.assertEqual(user_detail["account"], user_account)

        update_user_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/user/{user_id}",
            session_state=tenant_admin,
            json_body={
                "phone": self._build_phone("135", suffix),
                "realName": "自动化用户更新",
                "email": f"{user_account}.updated@example.com",
                "avatar": "https://example.com/avatar.png",
                "employeeType": 0,
            },
        )
        ensure_http_status(update_user_response, 200)
        ensure_api_status(update_user_response.json())

        org_delete_blocked_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/delete",
            session_state=tenant_admin,
            params={"id": department_id},
        )
        blocked_body = ensure_api_error(org_delete_blocked_response, 403, 11)
        self.assertIn("已关联用户", blocked_body["msg"])

        delete_user_safely(self.ctx, tenant_admin, user_id)

        delete_department_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/delete",
            session_state=tenant_admin,
            params={"id": department_id},
        )
        ensure_http_status(delete_department_response, 200)
        ensure_api_status(delete_department_response.json())

        delete_company_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/delete",
            session_state=tenant_admin,
            params={"id": company_id},
        )
        ensure_http_status(delete_company_response, 200)
        ensure_api_status(delete_company_response.json())

        delete_org_tree_safely(self.ctx, tenant_admin, tenant_root_org_id)

        delete_grant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/grant/app/delete",
            session_state=super_admin_session,
            params={"id": tenant_app_grant_id},
        )
        ensure_http_status(delete_grant_response, 200)
        ensure_api_status(delete_grant_response.json())

        delete_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant/delete",
            session_state=super_admin_session,
            params={"id": tenant_id},
        )
        ensure_http_status(delete_tenant_response, 200)
        ensure_api_status(delete_tenant_response.json())

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]

    def _find_org_node(self, nodes: list[dict], target_id: str):
        for node in nodes:
            data = node.get("data") or {}
            if data.get("id") == target_id:
                return node
            child = self._find_org_node(node.get("children") or [], target_id)
            if child is not None:
                return child
        return None
