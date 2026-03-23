from __future__ import annotations

import time

from common.assertions import ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class AppResourceFlowTestCase(BaseFlowTestCase):
    def test_app_menu_and_perm_crud_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))
        app_id = f"auto-app-{suffix}"
        app_name = f"自动化应用{suffix[-6:]}"
        menu_custom_id = f"auto-menu-{suffix}"
        perm_code = f"{app_id}:view"

        create_app_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/app",
            session_state=admin_session,
            json_body={
                "id": app_id,
                "nameCn": app_name,
                "memo": "auth-center python e2e app flow",
                "icon": "icon-app",
                "multiTenancy": True,
                "appType": 1,
                "appUrl": "",
            },
        )
        ensure_http_status(create_app_response, 200)
        created_app = ensure_api_status(create_app_response.json())
        self.assertEqual(created_app["id"], app_id)
        self.assertEqual(created_app["nameCn"], app_name)

        app_list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/app",
            session_state=admin_session,
        )
        ensure_http_status(app_list_response, 200)
        app_list = ensure_api_status(app_list_response.json())
        self.assertTrue(any(item["id"] == app_id for item in app_list), "应用列表中未找到新建应用")

        app_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/app/{app_id}",
            session_state=admin_session,
        )
        ensure_http_status(app_detail_response, 200)
        app_detail = ensure_api_status(app_detail_response.json())
        self.assertEqual(app_detail["id"], app_id)
        self.assertEqual(app_detail["nameCn"], app_name)

        update_app_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/app/{app_id}",
            session_state=admin_session,
            json_body={
                "nameCn": f"{app_name}-更新",
                "memo": "updated by python e2e",
                "icon": "icon-app-updated",
                "appUrl": "",
            },
        )
        ensure_http_status(update_app_response, 200)
        updated_app = ensure_api_status(update_app_response.json())
        self.assertEqual(updated_app["nameCn"], f"{app_name}-更新")

        create_menu_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/resource/tree",
            session_state=admin_session,
            json_body={
                "appId": app_id,
                "parentId": "",
                "nameCn": "自动化菜单",
                "resType": 0,
                "customId": menu_custom_id,
                "clientType": 0,
                "icon": "icon-menu",
                "routeLink": f"/auto/{suffix}",
                "hidden": 0,
                "showLevel": 0,
                "forbidden": 0,
                "subPerms": [
                    {
                        "permName": "查看",
                        "permCode": perm_code,
                        "apis": [
                            {
                                "apiMethod": 0,
                                "apiPath": f"/api/{app_id}/items",
                            }
                        ],
                    }
                ],
            },
        )
        ensure_http_status(create_menu_response, 200)
        created_menu = ensure_api_status(create_menu_response.json())
        menu_id = created_menu["id"]
        self.assertEqual(created_menu["appId"], app_id)

        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/resource/tree",
            session_state=admin_session,
            params={"appId": app_id, "withPerm": "true", "withApi": "true"},
        )
        ensure_http_status(tree_response, 200)
        tree_list = ensure_api_status(tree_response.json())
        created_node = self._find_tree_node(tree_list, menu_id)
        self.assertIsNotNone(created_node, "资源树中未找到新建菜单")
        self.assertEqual(created_node["data"]["res"]["customId"], menu_custom_id)

        menu_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/resource/tree/{menu_id}",
            session_state=admin_session,
        )
        ensure_http_status(menu_detail_response, 200)
        menu_detail = ensure_api_status(menu_detail_response.json())
        self.assertEqual(menu_detail["res"]["id"], menu_id)
        self.assertEqual(len(menu_detail["perms"]), 1)
        self.assertEqual(menu_detail["perms"][0]["permCode"], perm_code)
        perm_id = menu_detail["perms"][0]["id"]

        update_menu_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/resource/tree/{menu_id}",
            session_state=admin_session,
            json_body={
                "parentId": "",
                "nameCn": "自动化菜单-更新",
                "resType": 0,
                "customId": f"{menu_custom_id}-updated",
                "clientType": 0,
                "icon": "icon-menu-updated",
                "routeLink": f"/auto/{suffix}/updated",
                "hidden": 0,
                "showLevel": 0,
                "forbidden": 0,
                "subPerms": [
                    {
                        "id": perm_id,
                        "permName": "查看-更新",
                        "permCode": f"{perm_code}:updated",
                        "apis": [
                            {
                                "apiMethod": 1,
                                "apiPath": f"/api/{app_id}/items/query",
                            }
                        ],
                    },
                    {
                        "permName": "编辑",
                        "permCode": f"{app_id}:edit",
                        "apis": [
                            {
                                "apiMethod": 2,
                                "apiPath": f"/api/{app_id}/items/*",
                            }
                        ],
                    },
                ],
            },
        )
        ensure_http_status(update_menu_response, 200)
        updated_menu = ensure_api_status(update_menu_response.json())
        self.assertEqual(updated_menu["nameCn"], "自动化菜单-更新")
        self.assertEqual(updated_menu["customId"], f"{menu_custom_id}-updated")

        updated_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/resource/tree/{menu_id}",
            session_state=admin_session,
        )
        ensure_http_status(updated_detail_response, 200)
        updated_detail = ensure_api_status(updated_detail_response.json())
        self.assertEqual(updated_detail["res"]["nameCn"], "自动化菜单-更新")
        self.assertEqual(len(updated_detail["perms"]), 2)
        self.assertTrue(
            any(item["permCode"] == f"{perm_code}:updated" for item in updated_detail["perms"]),
            "更新后的权限点不存在",
        )
        self.assertTrue(
            any(item["permCode"] == f"{app_id}:edit" for item in updated_detail["perms"]),
            "新增的权限点不存在",
        )

        clear_perm_response = self.ctx.client.request(
            "PUT",
            f"/api/portal/v1/resource/tree/{menu_id}",
            session_state=admin_session,
            json_body={
                "parentId": "",
                "nameCn": "自动化菜单-更新",
                "resType": 0,
                "customId": f"{menu_custom_id}-updated",
                "clientType": 0,
                "icon": "icon-menu-updated",
                "routeLink": f"/auto/{suffix}/updated",
                "hidden": 0,
                "showLevel": 0,
                "forbidden": 0,
                "subPerms": [],
            },
        )
        ensure_http_status(clear_perm_response, 200)
        ensure_api_status(clear_perm_response.json())

        cleared_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/resource/tree/{menu_id}",
            session_state=admin_session,
        )
        ensure_http_status(cleared_detail_response, 200)
        cleared_detail = ensure_api_status(cleared_detail_response.json())
        self.assertEqual(len(cleared_detail["perms"]), 0)

        delete_menu_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/resource/tree/delete",
            session_state=admin_session,
            params={"id": menu_id, "force": "false"},
        )
        ensure_http_status(delete_menu_response, 200)
        ensure_api_status(delete_menu_response.json())

        tree_after_delete_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/resource/tree",
            session_state=admin_session,
            params={"appId": app_id, "withPerm": "true", "withApi": "true"},
        )
        ensure_http_status(tree_after_delete_response, 200)
        tree_after_delete = ensure_api_status(tree_after_delete_response.json())
        self.assertIsNone(self._find_tree_node(tree_after_delete, menu_id), "菜单删除后仍存在于资源树中")

        delete_app_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/app/delete",
            session_state=admin_session,
            params={"id": app_id},
        )
        ensure_http_status(delete_app_response, 200)
        ensure_api_status(delete_app_response.json())

        final_app_list_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/app",
            session_state=admin_session,
        )
        ensure_http_status(final_app_list_response, 200)
        final_app_list = ensure_api_status(final_app_list_response.json())
        self.assertFalse(any(item["id"] == app_id for item in final_app_list), "应用删除后列表仍可见")

    def _find_tree_node(self, nodes: list[dict], target_id: str):
        for node in nodes:
            data = node.get("data") or {}
            res = data.get("res") or {}
            if res.get("id") == target_id:
                return node
            child = self._find_tree_node(node.get("children") or [], target_id)
            if child is not None:
                return child
        return None
