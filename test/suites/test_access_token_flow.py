from __future__ import annotations

from common.assertions import ensure_api_error, ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class AccessTokenFlowTestCase(BaseFlowTestCase):
    def test_super_admin_access_token_crud_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")

        create_payload = {
            "keyName": "pytest-access-token",
            "forbidden": 0,
        }
        create_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/access-token",
            session_state=admin_session,
            json_body=create_payload,
        )
        ensure_http_status(create_response, 200)
        created = ensure_api_status(create_response.json())
        access_id = created["id"]

        list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/user/access-token",
            session_state=admin_session,
        )
        ensure_http_status(list_response, 200)
        listed = ensure_api_status(list_response.json())
        self.assertTrue(any(item["id"] == access_id for item in listed), "列表中未找到新建的 ak")

        detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/user/access-token/{access_id}",
            session_state=admin_session,
        )
        ensure_http_status(detail_response, 200)
        detail = ensure_api_status(detail_response.json())
        self.assertEqual(detail["id"], access_id)

        update_payload = {
            "keyName": "pytest-access-token-updated",
            "forbidden": 1,
        }
        update_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/user/access-token/{access_id}",
            session_state=admin_session,
            json_body=update_payload,
        )
        ensure_http_status(update_response, 200)
        updated = ensure_api_status(update_response.json())
        self.assertEqual(updated["keyName"], "pytest-access-token-updated")
        self.assertEqual(updated["forbidden"], 1)

        delete_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/access-token/delete",
            session_state=admin_session,
            params={"id": access_id},
        )
        ensure_http_status(delete_response, 200)
        ensure_api_status(delete_response.json())

        verify_deleted_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/user/access-token",
            session_state=admin_session,
        )
        ensure_http_status(verify_deleted_response, 200)
        remaining = ensure_api_status(verify_deleted_response.json())
        self.assertFalse(any(item["id"] == access_id for item in remaining), "删除后列表仍能查到 ak")

    def test_forbidden_user_cannot_create_access_token(self) -> None:
        forbidden_session = self.ctx.session("forbidden_user")
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/access-token",
            session_state=forbidden_session,
            json_body={"keyName": "forbidden-ak", "forbidden": 0},
        )
        body = ensure_api_error(response, 403, 11)
        self.assertIn("租户管理员", body["msg"])
