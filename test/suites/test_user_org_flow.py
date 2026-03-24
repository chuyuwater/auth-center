from __future__ import annotations

import time

from common.assertions import ensure_api_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class UserOrgFlowTestCase(BaseFlowTestCase):
    def test_user_org_assignment_and_switch_main_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("137", suffix)
        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_a_id = ""
        department_a_id = ""
        company_b_id = ""
        department_b_id = ""
        user_id = ""
        tenant_admin = None

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"任职租户{suffix[-6:]}",
                "shortName": f"任职{suffix[-4:]}",
                "logo": "",
                "memo": "user org flow tenant",
                "contactUser": "任职管理员",
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
                    name=f"user_org_tenant_admin_{tenant_id}",
                    login_field="account",
                    principal=tenant_phone,
                    password=tenant_phone,
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                )
            )

            invalid_add_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_admin,
                json_body={"userId": "", "nodeIds": []},
            )
            invalid_add_body = ensure_api_error(invalid_add_response, 400)
            self.assertTrue(
                "用户id不能为空" in invalid_add_body["msg"] or "组织id或部门id不能为空" in invalid_add_body["msg"],
                f"unexpected user/org add validation msg: {invalid_add_body['msg']}",
            )

            invalid_switch_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/switch-main",
                session_state=tenant_admin,
                json_body={"userId": "", "orgId": ""},
            )
            invalid_switch_body = ensure_api_error(invalid_switch_response, 400)
            self.assertIn("组织id不能为空", invalid_switch_body["msg"])

            company_a_id = self._create_org(
                tenant_admin,
                tenant_root_org_id,
                f"任职公司A{suffix[-4:]}",
                f"公A{suffix[-4:]}",
                node_type=0,
            )
            department_a_id = self._create_org(
                tenant_admin,
                company_a_id,
                f"任职部门A{suffix[-4:]}",
                f"部A{suffix[-4:]}",
                node_type=1,
            )
            company_b_id = self._create_org(
                tenant_admin,
                tenant_root_org_id,
                f"任职公司B{suffix[-4:]}",
                f"公B{suffix[-4:]}",
                node_type=0,
            )
            department_b_id = self._create_org(
                tenant_admin,
                company_b_id,
                f"任职部门B{suffix[-4:]}",
                f"部B{suffix[-4:]}",
                node_type=1,
            )

            create_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                json_body={
                    "nodeId": department_a_id,
                    "account": f"user_org_{suffix[-6:]}",
                    "phone": self._build_phone("179", suffix),
                    "realName": "任职用户",
                    "email": f"user_org_{suffix[-6:]}@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_http_status(create_user_response, 200)
            user_id = ensure_api_status(create_user_response.json())

            user_detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_id}",
                session_state=tenant_admin,
            )
            ensure_http_status(user_detail_response, 200)
            user_detail = ensure_api_status(user_detail_response.json())
            self.assertTrue(
                any(
                    item["nodeId"] == department_a_id and item["orgId"] == company_a_id and item["mainJob"] == 1
                    for item in user_detail["orgList"]
                ),
                "新建用户后应默认挂在首个部门，并将对应组织标记为主职",
            )

            add_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_admin,
                json_body={"userId": user_id, "nodeIds": [department_b_id]},
            )
            ensure_http_status(add_org_response, 200)
            ensure_api_status(add_org_response.json())

            detail_after_add_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_id}",
                session_state=tenant_admin,
            )
            ensure_http_status(detail_after_add_response, 200)
            detail_after_add = ensure_api_status(detail_after_add_response.json())
            self.assertTrue(
                any(
                    item["nodeId"] == department_b_id and item["orgId"] == company_b_id and item["mainJob"] == 0
                    for item in detail_after_add["orgList"]
                ),
                "新增兼职任职后未在详情中看到目标部门",
            )

            duplicate_add_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/add",
                session_state=tenant_admin,
                json_body={"userId": user_id, "nodeIds": [department_b_id]},
            )
            ensure_http_status(duplicate_add_response, 200)
            ensure_api_status(duplicate_add_response.json())

            detail_after_duplicate_add_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_id}",
                session_state=tenant_admin,
            )
            ensure_http_status(detail_after_duplicate_add_response, 200)
            detail_after_duplicate_add = ensure_api_status(detail_after_duplicate_add_response.json())
            self.assertEqual(
                sum(1 for item in detail_after_duplicate_add["orgList"] if item["nodeId"] == department_b_id),
                1,
                "重复新增任职后，同一部门下不应出现多条任职记录",
            )

            switch_main_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/switch-main",
                session_state=tenant_admin,
                json_body={"userId": user_id, "orgId": company_b_id},
            )
            ensure_http_status(switch_main_response, 200)
            ensure_api_status(switch_main_response.json())

            detail_after_switch_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_id}",
                session_state=tenant_admin,
            )
            ensure_http_status(detail_after_switch_response, 200)
            detail_after_switch = ensure_api_status(detail_after_switch_response.json())
            self.assertTrue(
                any(
                    item["orgId"] == company_b_id and item["mainJob"] == 1 for item in detail_after_switch["orgList"]
                ),
                "切换主职后公司B应成为主职组织",
            )
            self.assertTrue(
                any(
                    item["orgId"] == company_a_id and item["mainJob"] == 0 for item in detail_after_switch["orgList"]
                ),
                "切换主职后公司A应降为兼职组织",
            )

            delete_main_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/delete",
                session_state=tenant_admin,
                params={"userId": user_id, "nodeId": department_b_id},
            )
            delete_main_org_body = ensure_api_error(delete_main_org_response, 400, 10)
            self.assertIn("至少保留1个主职组织的任职", delete_main_org_body["msg"])

            missing_node_id_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/delete",
                session_state=tenant_admin,
                params={"userId": user_id},
            )
            missing_node_id_body = ensure_api_error(missing_node_id_response, 400)
            self.assertTrue(
                "节点id不能为空" in missing_node_id_body["msg"]
                or "参数无法绑定，请检查格式和字段" in missing_node_id_body["msg"],
                f"unexpected user/org delete validation msg: {missing_node_id_body['msg']}",
            )

            switch_back_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/switch-main",
                session_state=tenant_admin,
                json_body={"userId": user_id, "orgId": company_a_id},
            )
            ensure_http_status(switch_back_response, 200)
            ensure_api_status(switch_back_response.json())

            delete_part_time_org_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/org/delete",
                session_state=tenant_admin,
                params={"userId": user_id, "nodeId": department_b_id},
            )
            ensure_http_status(delete_part_time_org_response, 200)
            ensure_api_status(delete_part_time_org_response.json())

            final_detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/{user_id}",
                session_state=tenant_admin,
            )
            ensure_http_status(final_detail_response, 200)
            final_detail = ensure_api_status(final_detail_response.json())
            self.assertFalse(
                any(item["nodeId"] == department_b_id for item in final_detail["orgList"]),
                "删除兼职任职后，用户详情中仍能看到已移除部门",
            )
        finally:
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
                "memo": "user org flow node",
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
