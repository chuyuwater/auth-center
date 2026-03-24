from __future__ import annotations

import time

from common.assertions import ensure_api_status, ensure_http_status
from common.cleanup import delete_perm_unit_safely
from suites.base import BaseFlowTestCase


class PermUnitFlowTestCase(BaseFlowTestCase):
    def test_perm_unit_crud_and_grant_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        forbidden_session = self.ctx.session("forbidden_user")
        suffix = str(int(time.time() * 1000))
        group_id = ""
        unit_id = ""
        grant_ids: list[str] = []

        portal_app = self._get_portal_app(admin_session)
        self.assertEqual(portal_app["id"], "portal")

        portal_perm_ids = self._collect_portal_perm_candidates(admin_session)
        self.assertGreaterEqual(len(portal_perm_ids), 2, "portal 应用下至少需要两个权限点用于验权")
        granted_perm = portal_perm_ids[0]
        denied_perm = portal_perm_ids[1]

        initial_granted = self._check_perm(forbidden_session, granted_perm["code"])
        initial_denied = self._check_perm(forbidden_session, denied_perm["code"])
        self.assertFalse(initial_granted, "forbidden_user 初始不应拥有目标权限")
        self.assertFalse(initial_denied, "forbidden_user 初始不应拥有对照权限")

        try:
            create_group_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/group",
                session_state=admin_session,
                json_body={
                    "nodeName": f"自动化权限分组{suffix[-6:]}",
                    "memo": "perm unit group created by python e2e",
                    "parentId": "",
                    "policyModel": 0,
                },
            )
            ensure_http_status(create_group_response, 200)
            created_group = ensure_api_status(create_group_response.json())
            group_id = created_group["id"]
            self.assertEqual(created_group["nodeName"], f"自动化权限分组{suffix[-6:]}")

            group_tree_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/group/tree",
                session_state=admin_session,
            )
            ensure_http_status(group_tree_response, 200)
            group_tree = ensure_api_status(group_tree_response.json())
            self.assertIsNotNone(self._find_group_node(group_tree, group_id), "权限分组树中未找到新建节点")

            group_detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/perm/group/node/{group_id}",
                session_state=admin_session,
            )
            ensure_http_status(group_detail_response, 200)
            group_detail = ensure_api_status(group_detail_response.json())
            self.assertEqual(group_detail["id"], group_id)
            self.assertEqual(group_detail["nodeName"], f"自动化权限分组{suffix[-6:]}")

            update_group_response = self.ctx.client.request(
                "PUT",
                f"/api/portal/v1/perm/group/{group_id}",
                session_state=admin_session,
                json_body={
                    "nodeName": f"自动化权限分组{suffix[-6:]}-更新",
                    "memo": "perm unit group updated by python e2e",
                    "parentId": "",
                },
            )
            ensure_http_status(update_group_response, 200)
            updated_group = ensure_api_status(update_group_response.json())
            self.assertEqual(updated_group["nodeName"], f"自动化权限分组{suffix[-6:]}-更新")

            create_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit",
                session_state=admin_session,
                json_body={
                    "nameCn": f"自动化权限单元{suffix[-6:]}",
                    "memo": "perm unit created by python e2e",
                    "belongTo": group_id,
                },
            )
            ensure_http_status(create_unit_response, 200)
            created_unit = ensure_api_status(create_unit_response.json())
            unit_id = created_unit["id"]
            self.assertEqual(created_unit["belongTo"], group_id)

            list_unit_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit",
                session_state=admin_session,
                params={"page": 1, "size": 20, "name": f"自动化权限单元{suffix[-6:]}"},
            )
            ensure_http_status(list_unit_response, 200)
            listed_units = ensure_api_status(list_unit_response.json())
            self.assertTrue(any(item["id"] == unit_id for item in listed_units["list"]), "权限单元列表未查到新建数据")

            detail_unit_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/perm/unit/{unit_id}",
                session_state=admin_session,
            )
            ensure_http_status(detail_unit_response, 200)
            unit_detail = ensure_api_status(detail_unit_response.json())
            self.assertEqual(unit_detail["id"], unit_id)
            self.assertEqual(unit_detail["nameCn"], f"自动化权限单元{suffix[-6:]}")
            self.assertIn(unit_detail["forbidden"], (0, None))

            update_unit_response = self.ctx.client.request(
                "PUT",
                f"/api/portal/v1/perm/unit/{unit_id}",
                session_state=admin_session,
                json_body={
                    "nameCn": f"自动化权限单元{suffix[-6:]}-更新",
                    "memo": "perm unit updated by python e2e",
                    "belongTo": group_id,
                },
            )
            ensure_http_status(update_unit_response, 200)
            updated_unit = ensure_api_status(update_unit_response.json())
            self.assertEqual(updated_unit["nameCn"], f"自动化权限单元{suffix[-6:]}-更新")

            forbid_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/forbidden",
                session_state=admin_session,
                json_body={"unitId": unit_id, "forbidden": 1},
            )
            ensure_http_status(forbid_unit_response, 200)
            ensure_api_status(forbid_unit_response.json())

            forbidden_detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/perm/unit/{unit_id}",
                session_state=admin_session,
            )
            ensure_http_status(forbidden_detail_response, 200)
            forbidden_detail = ensure_api_status(forbidden_detail_response.json())
            self.assertEqual(forbidden_detail["forbidden"], 1)

            enable_unit_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/forbidden",
                session_state=admin_session,
                json_body={"unitId": unit_id, "forbidden": 0},
            )
            ensure_http_status(enable_unit_response, 200)
            ensure_api_status(enable_unit_response.json())

            packed_apps_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit/app",
                session_state=admin_session,
                params={"unitId": unit_id},
            )
            ensure_http_status(packed_apps_response, 200)
            packed_apps = ensure_api_status(packed_apps_response.json())
            portal_unit_app = next((item for item in packed_apps if item["appId"] == "portal"), None)
            self.assertIsNotNone(portal_unit_app, "权限单元应用列表中未找到 portal")
            self.assertFalse(portal_unit_app["packed"], "刚创建的权限单元不应已封装 portal 权限")

            pack_resource_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/resources",
                session_state=admin_session,
                json_body={
                    "unitId": unit_id,
                    "appId": "portal",
                    "permIds": [granted_perm["id"]],
                },
            )
            ensure_http_status(pack_resource_response, 200)
            ensure_api_status(pack_resource_response.json())

            packed_apps_after_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit/app",
                session_state=admin_session,
                params={"unitId": unit_id},
            )
            ensure_http_status(packed_apps_after_response, 200)
            packed_apps_after = ensure_api_status(packed_apps_after_response.json())
            portal_unit_app_after = next((item for item in packed_apps_after if item["appId"] == "portal"), None)
            self.assertIsNotNone(portal_unit_app_after)
            self.assertTrue(portal_unit_app_after["packed"], "封装权限后 portal 应标记为已封装")

            packed_resource_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit/resources",
                session_state=admin_session,
                params={"unitId": unit_id, "appId": "portal", "onlyPacked": "true"},
            )
            ensure_http_status(packed_resource_response, 200)
            packed_resources = ensure_api_status(packed_resource_response.json())
            packed_perm_codes = self._collect_packed_perm_codes(packed_resources)
            self.assertIn(granted_perm["code"], packed_perm_codes, "权限封装后未在权限树中看到目标权限")
            self.assertNotIn(denied_perm["code"], packed_perm_codes, "未封装的对照权限不应出现在已封装树中")

            pack_user_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/perm/unit/users",
                session_state=admin_session,
                json_body={
                    "unitIdList": [unit_id],
                    "userList": [
                        {
                            "userId": forbidden_session.user_id,
                            "orgId": forbidden_session.account.default_org_id,
                        }
                    ],
                },
            )
            ensure_http_status(pack_user_response, 200)
            ensure_api_status(pack_user_response.json())

            grant_user_list_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/perm/unit/users",
                session_state=admin_session,
                params={"page": 1, "size": 20, "unitId": unit_id},
            )
            ensure_http_status(grant_user_list_response, 200)
            grant_user_page = ensure_api_status(grant_user_list_response.json())
            unit_grants = [
                item for item in grant_user_page["list"] if item["userId"] == forbidden_session.user_id
            ]
            self.assertTrue(unit_grants, "封装人员后未在授权列表中看到 forbidden_user")
            grant_ids = [item["grantId"] for item in unit_grants]

            self.ctx.logout("forbidden_user")
            refreshed_forbidden_session = self.ctx.relogin("forbidden_user")

            granted_check_after_login = self._check_perm(refreshed_forbidden_session, granted_perm["code"])
            denied_check_after_login = self._check_perm(refreshed_forbidden_session, denied_perm["code"])
            self.assertTrue(granted_check_after_login, "重新登录后应拿到被授权权限")
            self.assertFalse(denied_check_after_login, "重新登录后不应拿到未授权权限")
        finally:
            if grant_ids:
                revoke_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/unit/users/delete-batch",
                    session_state=admin_session,
                    json_body={"ids": grant_ids},
                )
                ensure_http_status(revoke_response, 200)
                ensure_api_status(revoke_response.json())

                self.ctx.logout("forbidden_user")
                restored_forbidden_session = self.ctx.relogin("forbidden_user")
                self.assertFalse(
                    self._check_perm(restored_forbidden_session, granted_perm["code"]),
                    "撤销授权并重新登录后，forbidden_user 不应再拥有目标权限",
                )

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

    def _get_portal_app(self, admin_session) -> dict:
        app_list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/app",
            session_state=admin_session,
        )
        ensure_http_status(app_list_response, 200)
        app_list = ensure_api_status(app_list_response.json())
        portal_app = next((item for item in app_list if item["id"] == "portal"), None)
        self.assertIsNotNone(portal_app, "应用列表中未找到 portal")
        return portal_app

    def _collect_portal_perm_candidates(self, admin_session) -> list[dict[str, str]]:
        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/resource/tree",
            session_state=admin_session,
            params={"appId": "portal", "withPerm": "true", "withApi": "true"},
        )
        ensure_http_status(tree_response, 200)
        tree_list = ensure_api_status(tree_response.json())

        perms: list[dict[str, str]] = []
        self._walk_perm_tree(tree_list, perms)
        unique_perms: list[dict[str, str]] = []
        seen_codes: set[str] = set()
        for item in perms:
            if item["code"] in seen_codes:
                continue
            seen_codes.add(item["code"])
            unique_perms.append(item)
        return unique_perms

    def _walk_perm_tree(self, nodes: list[dict], perms: list[dict[str, str]]) -> None:
        for node in nodes:
            data = node.get("data") or {}
            perm = data.get("perm") or {}
            if perm.get("id") and perm.get("permCode"):
                perms.append({"id": perm["id"], "code": perm["permCode"]})
            self._walk_perm_tree(node.get("children") or [], perms)

    def _collect_packed_perm_codes(self, nodes: list[dict]) -> set[str]:
        codes: set[str] = set()
        for node in nodes:
            data = node.get("data") or {}
            perm = data.get("perm") or {}
            if perm.get("permCode"):
                codes.add(perm["permCode"])
            codes.update(self._collect_packed_perm_codes(node.get("children") or []))
        return codes

    def _check_perm(self, session_state, perm_code: str) -> bool:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/client/perm-check",
            session_state=session_state,
            params={"permCode": perm_code},
        )
        ensure_http_status(response, 200)
        return bool(ensure_api_status(response.json()))

    def _find_group_node(self, nodes: list[dict], target_id: str):
        for node in nodes:
            data = node.get("data") or {}
            if data.get("id") == target_id:
                return node
            child = self._find_group_node(node.get("children") or [], target_id)
            if child is not None:
                return child
        return None
