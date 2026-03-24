from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.cleanup import delete_perm_unit_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class ValidationFlowTestCase(BaseFlowTestCase):
    def test_tenant_validation_and_duplicate_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_id = ""
        tenant_name = f"校验租户{suffix[-6:]}"
        contact_phone = self._build_phone("137", suffix)

        missing_name_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            json_body={
                "nameCn": "",
                "shortName": f"租户{suffix[-4:]}",
                "logo": "",
                "memo": "tenant validation missing name",
                "contactUser": "校验联系人",
                "contactPhone": contact_phone,
            },
        )
        ensure_api_client_error(missing_name_response)

        invalid_phone_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            json_body={
                "nameCn": tenant_name,
                "shortName": f"租户{suffix[-4:]}",
                "logo": "",
                "memo": "tenant validation invalid phone",
                "contactUser": "校验联系人",
                "contactPhone": "12345",
            },
        )
        ensure_api_client_error(invalid_phone_response)

        create_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=admin_session,
            json_body={
                "nameCn": tenant_name,
                "shortName": f"租户{suffix[-4:]}",
                "logo": "",
                "memo": "tenant validation baseline",
                "contactUser": "校验联系人",
                "contactPhone": contact_phone,
            },
        )
        ensure_http_status(create_response, 200)
        created = ensure_api_status(create_response.json())
        tenant_id = created["id"]

        try:
            duplicate_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/tenant",
                session_state=admin_session,
                json_body={
                    "nameCn": tenant_name,
                    "shortName": f"重复{suffix[-4:]}",
                    "logo": "",
                    "memo": "tenant validation duplicate",
                    "contactUser": "重复联系人",
                    "contactPhone": self._build_phone("138", suffix),
                },
            )
            ensure_api_client_error(duplicate_response, 10)
        finally:
            if tenant_id:
                delete_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/tenant/delete",
                    session_state=admin_session,
                    params={"id": tenant_id},
                )
                ensure_http_status(delete_response, 200)
                ensure_api_status(delete_response.json())

    def test_app_validation_and_duplicate_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        app_id = f"auto-val-app-{suffix}"
        app_name = f"校验应用{suffix[-6:]}"

        missing_id_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/app",
            session_state=admin_session,
            json_body={
                "id": "",
                "nameCn": app_name,
                "memo": "app validation missing id",
                "icon": "icon-app",
                "multiTenancy": True,
                "appType": 1,
                "appUrl": "",
            },
        )
        ensure_api_client_error(missing_id_response)

        create_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/app",
            session_state=admin_session,
            json_body={
                "id": app_id,
                "nameCn": app_name,
                "memo": "app validation baseline",
                "icon": "icon-app",
                "multiTenancy": True,
                "appType": 1,
                "appUrl": "",
            },
        )
        ensure_http_status(create_response, 200)
        created = ensure_api_status(create_response.json())
        self.assertEqual(created["id"], app_id)

        try:
            duplicate_id_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/app",
                session_state=admin_session,
                json_body={
                    "id": app_id,
                    "nameCn": f"重复应用{suffix[-6:]}",
                    "memo": "app validation duplicate id",
                    "icon": "icon-app",
                    "multiTenancy": True,
                    "appType": 1,
                    "appUrl": "",
                },
            )
            ensure_api_client_error(duplicate_id_response, 10)
        finally:
            delete_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/app/delete",
                session_state=admin_session,
                params={"id": app_id},
            )
            ensure_http_status(delete_response, 200)
            ensure_api_status(delete_response.json())

    def test_org_and_user_validation_and_duplicate_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        temp_user_id = ""

        invalid_org_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=admin_session,
            json_body={
                "nodeName": f"校验组织{suffix[-4:]}",
                "shortName": "",
                "memo": "org validation",
                "parentId": admin_session.account.default_org_id,
                "nodeCategory": 1,
                "existType": 0,
                "nodeType": 2,
            },
        )
        ensure_api_client_error(invalid_org_response)

        invalid_user_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user",
            session_state=admin_session,
            json_body={
                "nodeId": admin_session.account.default_org_id,
                "account": "ab",
                "phone": "12345",
                "realName": "A",
                "email": "invalid-email",
                "avatar": "",
                "employeeType": 0,
            },
        )
        ensure_api_client_error(invalid_user_response)

        user_account = f"auto_val_user_{suffix[-6:]}"
        user_phone = self._build_phone("179", suffix)
        create_user_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user",
            session_state=admin_session,
            json_body={
                "nodeId": admin_session.account.default_org_id,
                "account": user_account,
                "phone": user_phone,
                "realName": "校验用户",
                "email": f"{user_account}@example.com",
                "avatar": "",
                "employeeType": 0,
            },
        )
        ensure_http_status(create_user_response, 200)
        temp_user_id = ensure_api_status(create_user_response.json())

        try:
            duplicate_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=admin_session,
                json_body={
                    "nodeId": admin_session.account.default_org_id,
                    "account": user_account,
                    "phone": self._build_phone("178", suffix),
                    "realName": "重复用户",
                    "email": f"{user_account}.dup@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_api_client_error(duplicate_user_response, 10)
        finally:
            if temp_user_id:
                delete_user_safely(self.ctx, admin_session, temp_user_id)

    def test_perm_validation_and_duplicate_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        group_id = ""
        unit_id = ""

        invalid_group_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/perm/group",
            session_state=admin_session,
            json_body={"nodeName": "", "memo": "invalid perm group", "parentId": "", "policyModel": 0},
        )
        ensure_api_client_error(invalid_group_response)

        create_group_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/perm/group",
            session_state=admin_session,
            json_body={
                "nodeName": f"校验权限分组{suffix[-6:]}",
                "memo": "perm validation baseline",
                "parentId": "",
                "policyModel": 0,
            },
        )
        ensure_http_status(create_group_response, 200)
        created_group = ensure_api_status(create_group_response.json())
        group_id = created_group["id"]

        try:
            duplicate_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=admin_session,
                json_body={
                    "nodeName": f"校验权限分组{suffix[-6:]}",
                    "memo": "perm validation duplicate group",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_api_client_error(duplicate_group_response, 10)

            invalid_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=admin_session,
                json_body={"nameCn": "", "memo": "invalid perm unit", "belongTo": ""},
            )
            ensure_api_client_error(invalid_unit_response)

            create_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=admin_session,
                json_body={
                    "nameCn": f"校验权限单元{suffix[-6:]}",
                    "memo": "perm validation baseline",
                    "belongTo": group_id,
                },
            )
            ensure_http_status(create_unit_response, 200)
            created_unit = ensure_api_status(create_unit_response.json())
            unit_id = created_unit["id"]

            duplicate_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=admin_session,
                json_body={
                    "nameCn": f"校验权限单元{suffix[-6:]}",
                    "memo": "perm validation duplicate unit",
                    "belongTo": group_id,
                },
            )
            ensure_api_client_error(duplicate_unit_response, 10)
        finally:
            if unit_id:
                delete_perm_unit_safely(self.ctx, admin_session, unit_id)
            if group_id:
                delete_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group/delete",
                    session_state=admin_session,
                    params={"id": group_id},
                )
                ensure_http_status(delete_group_response, 200)
                ensure_api_status(delete_group_response.json())

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        return prefix + seed[-8:]
