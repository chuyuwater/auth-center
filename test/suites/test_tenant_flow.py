from __future__ import annotations

import time

from common.assertions import ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class TenantFlowTestCase(BaseFlowTestCase):
    def test_tenant_crud_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_name = f"自动化租户{suffix[-6:]}"
        create_phone = self._build_phone("139", suffix)
        update_phone = self._build_phone("138", suffix)

        create_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            json_body={
                "nameCn": tenant_name,
                "shortName": f"租户{suffix[-4:]}",
                "logo": "https://example.com/logo-create.png",
                "memo": "tenant created by python e2e",
                "contactUser": "自动化联系人",
                "contactPhone": create_phone,
            },
        )
        ensure_http_status(create_response, 200)
        created = ensure_api_status(create_response.json())
        tenant_id = created["id"]
        self.assertTrue(tenant_id, "租户id为空")
        self.assertEqual(created["nameCn"], tenant_name)
        self.assertTrue(created["adminId"], "默认管理员id为空")

        list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            params={"page": 1, "size": 20, "keyword": tenant_name},
        )
        ensure_http_status(list_response, 200)
        listed = ensure_api_status(list_response.json())
        self.assertTrue(any(item["id"] == tenant_id for item in listed["list"]), "租户列表未查到新建租户")

        detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/tenant/{tenant_id}",
            session_state=admin_session,
        )
        ensure_http_status(detail_response, 200)
        detail = ensure_api_status(detail_response.json())
        self.assertEqual(detail["id"], tenant_id)
        self.assertEqual(detail["contactPhone"], create_phone)

        update_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/tenant/{tenant_id}",
            session_state=admin_session,
            json_body={
                "nameCn": f"{tenant_name}-更新",
                "shortName": f"更新{suffix[-4:]}",
                "logo": "https://example.com/logo-updated.png",
                "memo": "tenant updated by python e2e",
                "contactUser": "更新联系人",
                "contactPhone": update_phone,
            },
        )
        ensure_http_status(update_response, 200)
        updated = ensure_api_status(update_response.json())
        self.assertEqual(updated["nameCn"], f"{tenant_name}-更新")
        self.assertEqual(updated["contactPhone"], update_phone)

        updated_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/tenant/{tenant_id}",
            session_state=admin_session,
        )
        ensure_http_status(updated_detail_response, 200)
        updated_detail = ensure_api_status(updated_detail_response.json())
        self.assertEqual(updated_detail["nameCn"], f"{tenant_name}-更新")
        self.assertEqual(updated_detail["forbidden"], 0)

        disable_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant/status",
            session_state=admin_session,
            json_body={"tenantId": tenant_id, "forbidden": 1},
        )
        ensure_http_status(disable_response, 200)
        ensure_api_status(disable_response.json())

        disabled_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/tenant/{tenant_id}",
            session_state=admin_session,
        )
        ensure_http_status(disabled_detail_response, 200)
        disabled_detail = ensure_api_status(disabled_detail_response.json())
        self.assertEqual(disabled_detail["forbidden"], 1)

        enable_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant/status",
            session_state=admin_session,
            json_body={"tenantId": tenant_id, "forbidden": 0},
        )
        ensure_http_status(enable_response, 200)
        ensure_api_status(enable_response.json())

        enabled_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/tenant/{tenant_id}",
            session_state=admin_session,
        )
        ensure_http_status(enabled_detail_response, 200)
        enabled_detail = ensure_api_status(enabled_detail_response.json())
        self.assertEqual(enabled_detail["forbidden"], 0)

        delete_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant/delete",
            session_state=admin_session,
            params={"id": tenant_id},
        )
        ensure_http_status(delete_response, 200)
        ensure_api_status(delete_response.json())

        deleted_list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            params={"page": 1, "size": 20, "keyword": f"{tenant_name}-更新"},
        )
        ensure_http_status(deleted_list_response, 200)
        deleted_list = ensure_api_status(deleted_list_response.json())
        self.assertFalse(any(item["id"] == tenant_id for item in deleted_list["list"]), "删除后租户仍出现在列表中")

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
