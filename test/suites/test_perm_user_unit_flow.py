from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely, delete_perm_unit_safely, delete_user_safely
from suites.base import BaseFlowTestCase


class PermUserUnitFlowTestCase(BaseFlowTestCase):
    def test_perm_user_units_crud_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        tenant_phone = self._build_phone("138", suffix)

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        company_id = ""
        department_id = ""
        user_id = ""
        group_id = ""
        unit_ids: list[str] = []
        tenant_admin = None

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"用户角色租户{suffix[-6:]}",
                "shortName": f"用户角{suffix[-4:]}",
                "logo": "",
                "memo": "perm user units flow tenant",
                "contactUser": "角色管理员",
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
                    name=f"perm_user_unit_admin_{tenant_id}",
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
                f"角色公司{suffix[-4:]}",
                f"角公{suffix[-4:]}",
                0,
            )
            department_id = self._create_org(
                tenant_admin,
                company_id,
                f"角色部门{suffix[-4:]}",
                f"角部{suffix[-4:]}",
                1,
            )

            create_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user",
                session_state=tenant_admin,
                json_body={
                    "nodeId": department_id,
                    "account": f"perm_user_{suffix[-6:]}",
                    "phone": self._build_phone("179", suffix),
                    "realName": "权限单元用户",
                    "email": f"perm_user_{suffix[-6:]}@example.com",
                    "avatar": "",
                    "employeeType": 0,
                },
            )
            ensure_http_status(create_user_response, 200)
            user_id = ensure_api_status(create_user_response.json())

            invalid_list_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
            )
            ensure_api_client_error(invalid_list_response)

            invalid_add_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                json_body={"unitIdList": [], "userList": []},
            )
            ensure_api_client_error(invalid_add_response)

            create_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=tenant_admin,
                json_body={
                    "nodeName": f"用户角色分组{suffix[-4:]}",
                    "memo": "perm user units flow group",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_http_status(create_group_response, 200)
            created_group = ensure_api_status(create_group_response.json())
            group_id = created_group["id"]

            for unit_name in (f"用户角色A{suffix[-4:]}", f"用户角色B{suffix[-4:]}"):
                create_unit_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/unit",
                    session_state=tenant_admin,
                    json_body={"nameCn": unit_name, "memo": "perm user units flow unit", "belongTo": group_id},
                )
                ensure_http_status(create_unit_response, 200)
                created_unit = ensure_api_status(create_unit_response.json())
                unit_ids.append(created_unit["id"])

            add_units_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                json_body={
                    "unitIdList": unit_ids,
                    "userList": [{"userId": user_id, "orgId": company_id}],
                },
            )
            ensure_http_status(add_units_response, 200)
            ensure_api_status(add_units_response.json())

            list_units_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                params={"userId": user_id, "orgId": company_id},
            )
            ensure_http_status(list_units_response, 200)
            listed_units = ensure_api_status(list_units_response.json())
            self.assertEqual(
                sorted(item["unitId"] for item in listed_units if item["unitId"] in unit_ids),
                sorted(unit_ids),
                "按用户查询权限单元时未返回完整授权清单",
            )

            duplicate_add_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                json_body={
                    "unitIdList": [unit_ids[0]],
                    "userList": [{"userId": user_id, "orgId": company_id}],
                },
            )
            ensure_http_status(duplicate_add_response, 200)
            ensure_api_status(duplicate_add_response.json())

            detail_after_duplicate_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                params={"userId": user_id, "orgId": company_id, "unitId": unit_ids[0]},
            )
            ensure_http_status(detail_after_duplicate_response, 200)
            detail_after_duplicate = ensure_api_status(detail_after_duplicate_response.json())
            self.assertEqual(
                sum(1 for item in detail_after_duplicate if item["unitId"] == unit_ids[0]),
                1,
                "重复授权后，同一用户-组织-权限单元组合不应出现多条记录",
            )

            first_grant_id = next(item["grantId"] for item in listed_units if item["unitId"] == unit_ids[0])
            second_grant_id = next(item["grantId"] for item in listed_units if item["unitId"] == unit_ids[1])

            invalid_delete_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units/delete",
                session_state=tenant_admin,
            )
            ensure_api_client_error(invalid_delete_response)

            delete_one_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units/delete",
                session_state=tenant_admin,
                params={"id": first_grant_id},
            )
            ensure_http_status(delete_one_response, 200)
            ensure_api_status(delete_one_response.json())

            list_after_delete_one_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                params={"userId": user_id, "orgId": company_id},
            )
            ensure_http_status(list_after_delete_one_response, 200)
            list_after_delete_one = ensure_api_status(list_after_delete_one_response.json())
            self.assertEqual(
                [item["unitId"] for item in list_after_delete_one if item["unitId"] in unit_ids],
                [unit_ids[1]],
                "删除单条授权后，应仅剩第二条授权",
            )

            delete_batch_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/user/units/delete-batch",
                session_state=tenant_admin,
                json_body={"ids": [second_grant_id]},
            )
            ensure_http_status(delete_batch_response, 200)
            ensure_api_status(delete_batch_response.json())

            final_list_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/user/units",
                session_state=tenant_admin,
                params={"userId": user_id, "orgId": company_id},
            )
            ensure_http_status(final_list_response, 200)
            final_list = ensure_api_status(final_list_response.json())
            self.assertFalse(
                any(item["unitId"] in unit_ids for item in final_list),
                "批量删除授权后，用户不应再关联任何测试权限单元",
            )
        finally:
            for unit_id in reversed(unit_ids):
                if tenant_admin is not None:
                    delete_perm_unit_safely(self.ctx, tenant_admin, unit_id)

            if group_id and tenant_admin is not None:
                delete_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group/delete",
                    session_state=tenant_admin,
                    params={"id": group_id},
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

    def _create_org(self, session_state, parent_id: str, node_name: str, short_name: str, node_type: int) -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=session_state,
            json_body={
                "nodeName": node_name,
                "shortName": short_name,
                "memo": "perm user units flow org",
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
