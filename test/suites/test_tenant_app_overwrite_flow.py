from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class TenantAppOverwriteFlowTestCase(BaseFlowTestCase):
    def test_tenant_app_overwrite_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_id = ""
        app_ids: list[str] = []

        missing_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/grant/app/overwrite",
            session_state=admin_session,
            json_body={"tenantId": "", "appIds": ["portal"]},
        )
        ensure_api_client_error(missing_tenant_response)

        missing_app_ids_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/grant/app/overwrite",
            session_state=admin_session,
            json_body={"tenantId": "0", "appIds": []},
        )
        ensure_api_client_error(missing_app_ids_response)

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            json_body={
                "nameCn": f"覆盖授权租户{suffix[-6:]}",
                "shortName": f"覆盖{suffix[-4:]}",
                "logo": "",
                "memo": "tenant app overwrite flow",
                "contactUser": "覆盖管理员",
                "contactPhone": self._build_phone("137", suffix),
            },
        )
        ensure_http_status(create_tenant_response, 200)
        tenant = ensure_api_status(create_tenant_response.json())
        tenant_id = tenant["id"]

        app_a_id = f"overwrite-app-a-{suffix}"
        app_b_id = f"overwrite-app-b-{suffix}"
        app_ids = [app_a_id, app_b_id]

        try:
            for app_id, app_name in (
                (app_a_id, f"覆盖应用A{suffix[-4:]}"),
                (app_b_id, f"覆盖应用B{suffix[-4:]}"),
            ):
                create_app_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/app",
                    session_state=admin_session,
                    json_body={
                        "id": app_id,
                        "nameCn": app_name,
                        "memo": "tenant app overwrite flow app",
                        "icon": "icon-app",
                        "multiTenancy": True,
                        "appType": 1,
                        "appUrl": "",
                    },
                )
                ensure_http_status(create_app_response, 200)
                created_app = ensure_api_status(create_app_response.json())
                self.assertEqual(created_app["id"], app_id)

            invalid_app_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app/overwrite",
                session_state=admin_session,
                json_body={"tenantId": tenant_id, "appIds": [app_a_id, "missing-app-id"]},
            )
            ensure_api_client_error(invalid_app_response, 10)

            first_overwrite_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app/overwrite",
                session_state=admin_session,
                json_body={"tenantId": tenant_id, "appIds": [app_a_id]},
            )
            ensure_http_status(first_overwrite_response, 200)
            ensure_api_status(first_overwrite_response.json())

            first_list_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/grant/app/{tenant_id}",
                session_state=admin_session,
            )
            ensure_http_status(first_list_response, 200)
            first_grants = ensure_api_status(first_list_response.json())
            self.assertEqual(
                sorted(item["appId"] for item in first_grants),
                [app_a_id],
                "第一次覆盖授权后，租户应只保留应用A",
            )

            second_overwrite_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/grant/app/overwrite",
                session_state=admin_session,
                json_body={"tenantId": tenant_id, "appIds": [app_b_id]},
            )
            ensure_http_status(second_overwrite_response, 200)
            ensure_api_status(second_overwrite_response.json())

            second_list_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/grant/app/{tenant_id}",
                session_state=admin_session,
            )
            ensure_http_status(second_list_response, 200)
            second_grants = ensure_api_status(second_list_response.json())
            self.assertEqual(
                sorted(item["appId"] for item in second_grants),
                [app_b_id],
                "第二次覆盖授权后，应用A应被移除，只保留应用B",
            )
        finally:
            if tenant_id:
                list_grant_apps_response = self.ctx.client.request(
                    "GET",
                    f"/api/portal/v1/grant/app/{tenant_id}",
                    session_state=admin_session,
                )
                ensure_http_status(list_grant_apps_response, 200)
                grant_apps = ensure_api_status(list_grant_apps_response.json())
                for item in grant_apps:
                    delete_grant_response = self.ctx.client.request(
                        "POST",
                        "/api/portal/v1/grant/app/delete",
                        session_state=admin_session,
                        params={"id": item["grantId"]},
                    )
                    ensure_http_status(delete_grant_response, 200)
                    ensure_api_status(delete_grant_response.json())

            for app_id in app_ids:
                delete_app_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/app/delete",
                    session_state=admin_session,
                    params={"id": app_id},
                )
                ensure_http_status(delete_app_response, 200)
                ensure_api_status(delete_app_response.json())

            if tenant_id:
                delete_tenant_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/tenant/delete",
                    session_state=admin_session,
                    params={"id": tenant_id},
                )
                ensure_http_status(delete_tenant_response, 200)
                ensure_api_status(delete_tenant_response.json())

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
