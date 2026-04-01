from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from common.client import AccountConfig
from common.cleanup import delete_org_tree_safely
from suites.base import BaseFlowTestCase


class MoveFlowTestCase(BaseFlowTestCase):
    def test_app_org_perm_group_and_resource_move_flow(self) -> None:
        super_admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))

        tenant_id = ""
        tenant_root_org_id = ""
        tenant_app_grant_id = ""
        tenant_admin = None
        app_ids: list[str] = []
        group_ids: list[str] = []
        menu_ids: list[str] = []

        self._assert_common_move_validation(super_admin_session)

        create_tenant_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/tenant",
            session_state=super_admin_session,
            json_body={
                "nameCn": f"拖动租户{suffix[-6:]}",
                "shortName": f"拖动{suffix[-4:]}",
                "logo": "",
                "memo": "move flow tenant",
                "contactUser": "拖动管理员",
                "contactPhone": self._build_phone("137", suffix),
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
                    name=f"move_tenant_admin_{tenant_id}",
                    login_field="account",
                    principal=self._build_phone("137", suffix),
                    password=self._build_phone("137", suffix),
                    tenant_id=tenant_id,
                    org_ids=[tenant_root_org_id],
                    extra_headers={},
                )
            )

            self._test_app_move(super_admin_session, suffix, app_ids)
            self._test_perm_group_move(super_admin_session, suffix, group_ids)
            self._test_resource_move(super_admin_session, suffix, app_ids, menu_ids)
            self._test_org_move(tenant_admin, tenant_root_org_id, suffix)
        finally:
            for menu_id, app_id in reversed(menu_ids):
                delete_menu_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/resource/tree/delete",
                    session_state=super_admin_session,
                    params={"id": menu_id},
                )
                ensure_http_status(delete_menu_response, 200)
                ensure_api_status(delete_menu_response.json())

            for group_id in reversed(group_ids):
                delete_group_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/perm/group/delete",
                    session_state=super_admin_session,
                    params={"id": group_id},
                )
                ensure_http_status(delete_group_response, 200)
                ensure_api_status(delete_group_response.json())

            for app_id in reversed(app_ids):
                delete_app_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/app/delete",
                    session_state=super_admin_session,
                    params={"id": app_id},
                )
                ensure_http_status(delete_app_response, 200)
                ensure_api_status(delete_app_response.json())

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

    def _assert_common_move_validation(self, session_state) -> None:
        invalid_move_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/app/move",
            session_state=session_state,
            json_body={"nodeId": "", "parentId": "", "prevId": ""},
        )
        ensure_api_client_error(invalid_move_response)

        same_node_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/app/move",
            session_state=session_state,
            json_body={"nodeId": "portal", "parentId": "", "prevId": "portal"},
        )
        ensure_api_client_error(same_node_response, 10)

    def _test_app_move(self, session_state, suffix: str, app_ids: list[str]) -> None:
        app_a_id = f"move-app-a-{suffix}"
        app_b_id = f"move-app-b-{suffix}"
        for app_id, app_name in ((app_a_id, f"拖动应用A{suffix[-4:]}"), (app_b_id, f"拖动应用B{suffix[-4:]}")):
            create_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/app",
                session_state=session_state,
                json_body={
                    "id": app_id,
                    "nameCn": app_name,
                    "memo": "move flow app",
                    "icon": "icon-app",
                    "multiTenancy": True,
                    "appType": 1,
                    "appUrl": "",
                },
            )
            ensure_http_status(create_response, 200)
            ensure_api_status(create_response.json())
            app_ids.append(app_id)

        missing_prev_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/app/move",
            session_state=session_state,
            json_body={"nodeId": app_b_id, "parentId": "", "prevId": "missing-app"},
        )
        ensure_api_client_error(missing_prev_response, 10)

        move_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/app/move",
            session_state=session_state,
            json_body={"nodeId": app_b_id, "parentId": "", "prevId": app_a_id},
        )
        ensure_http_status(move_response, 200)
        ensure_api_status(move_response.json())

        list_response = self.ctx.client.request("GET", "/api/portal/v1/app", session_state=session_state)
        ensure_http_status(list_response, 200)
        app_list = ensure_api_status(list_response.json())
        filtered_ids = [item["id"] for item in app_list if item["id"] in {app_a_id, app_b_id}]
        self.assertEqual(filtered_ids, [app_a_id, app_b_id])

    def _test_perm_group_move(self, session_state, suffix: str, group_ids: list[str]) -> None:
        group_a_id = self._create_perm_group(session_state, f"拖动权限组A{suffix[-4:]}")
        group_b_id = self._create_perm_group(session_state, f"拖动权限组B{suffix[-4:]}")
        child_group_id = self._create_perm_group(session_state, f"拖动权限子组{suffix[-4:]}", parent_id=group_a_id)
        group_ids.extend([group_a_id, group_b_id, child_group_id])

        missing_prev_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/perm/group/move",
            session_state=session_state,
            json_body={"nodeId": group_b_id, "parentId": "", "prevId": "missing-group"},
        )
        ensure_api_client_error(missing_prev_response, 10)

        child_cycle_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/perm/group/move",
            session_state=session_state,
            json_body={"nodeId": group_a_id, "parentId": child_group_id, "prevId": ""},
        )
        ensure_api_client_error(child_cycle_response, 10)

        move_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/perm/group/move",
            session_state=session_state,
            json_body={"nodeId": group_b_id, "parentId": "", "prevId": group_a_id},
        )
        ensure_http_status(move_response, 200)
        ensure_api_status(move_response.json())

        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/perm/group/tree",
            session_state=session_state,
        )
        ensure_http_status(tree_response, 200)
        tree = ensure_api_status(tree_response.json())
        filtered_ids = [node["data"]["id"] for node in tree if node["data"]["id"] in {group_a_id, group_b_id}]
        self.assertEqual(filtered_ids, [group_a_id, group_b_id])

    def _test_resource_move(self, session_state, suffix: str, app_ids: list[str], menu_ids: list[tuple[str, str]]) -> None:
        app_id = f"move-res-app-{suffix}"
        other_app_id = f"move-res-app-other-{suffix}"
        for current_app_id, current_app_name in (
            (app_id, f"拖动菜单应用{suffix[-4:]}"),
            (other_app_id, f"拖动菜单他应用{suffix[-4:]}"),
        ):
            create_app_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/app",
                session_state=session_state,
                json_body={
                    "id": current_app_id,
                    "nameCn": current_app_name,
                    "memo": "move flow resource app",
                    "icon": "icon-app",
                    "multiTenancy": True,
                    "appType": 1,
                    "appUrl": "",
                },
            )
            ensure_http_status(create_app_response, 200)
            ensure_api_status(create_app_response.json())
            app_ids.append(current_app_id)

        menu_a_id = self._create_menu(session_state, app_id, f"菜单A{suffix[-4:]}", f"move-menu-a-{suffix}")
        menu_b_id = self._create_menu(session_state, app_id, f"菜单B{suffix[-4:]}", f"move-menu-b-{suffix}")
        child_menu_id = self._create_menu(
            session_state,
            app_id,
            f"菜单子节点{suffix[-4:]}",
            f"move-menu-child-{suffix}",
            parent_id=menu_a_id,
        )
        other_app_menu_id = self._create_menu(
            session_state,
            other_app_id,
            f"他应用菜单{suffix[-4:]}",
            f"move-menu-other-{suffix}",
        )
        menu_ids.extend(
            [(menu_a_id, app_id), (menu_b_id, app_id), (child_menu_id, app_id), (other_app_menu_id, other_app_id)]
        )

        missing_prev_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/resource/tree/move",
            session_state=session_state,
            json_body={"nodeId": menu_b_id, "parentId": "", "prevId": "missing-menu"},
        )
        ensure_api_client_error(missing_prev_response, 10)

        child_cycle_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/resource/tree/move",
            session_state=session_state,
            json_body={"nodeId": menu_a_id, "parentId": child_menu_id, "prevId": ""},
        )
        ensure_api_client_error(child_cycle_response, 10)

        cross_app_parent_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/resource/tree/move",
            session_state=session_state,
            json_body={"nodeId": menu_a_id, "parentId": other_app_menu_id, "prevId": ""},
        )
        ensure_api_client_error(cross_app_parent_response, 10)

        move_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/resource/tree/move",
            session_state=session_state,
            json_body={"nodeId": menu_b_id, "parentId": "", "prevId": menu_a_id},
        )
        ensure_http_status(move_response, 200)
        ensure_api_status(move_response.json())

        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/resource/tree",
            session_state=session_state,
            params={"appId": app_id},
        )
        ensure_http_status(tree_response, 200)
        tree = ensure_api_status(tree_response.json())
        filtered_ids = [node["data"]["res"]["id"] for node in tree if node["data"]["res"]["id"] in {menu_a_id, menu_b_id}]
        self.assertEqual(filtered_ids, [menu_a_id, menu_b_id])

    def _test_org_move(self, session_state, root_org_id: str, suffix: str) -> None:
        company_a_id = self._create_org(session_state, root_org_id, f"拖动公司A{suffix[-4:]}", f"拖A{suffix[-4:]}", 0)
        company_b_id = self._create_org(session_state, root_org_id, f"拖动公司B{suffix[-4:]}", f"拖B{suffix[-4:]}", 0)
        department_id = self._create_org(session_state, company_a_id, f"拖动部门{suffix[-4:]}", f"拖部{suffix[-4:]}", 1)

        move_root_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/org/node/move",
            session_state=session_state,
            json_body={"nodeId": root_org_id, "parentId": "", "prevId": ""},
        )
        ensure_http_status(move_root_response, 200)
        ensure_api_status(move_root_response.json())

        root_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/org/node/{root_org_id}",
            session_state=session_state,
        )
        ensure_http_status(root_detail_response, 200)
        root_detail = ensure_api_status(root_detail_response.json())
        self.assertEqual(
            root_detail["id"],
            root_org_id,
            "默认根组织执行 move 后应仍保持可查询",
        )

        missing_prev_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/org/node/move",
            session_state=session_state,
            json_body={"nodeId": company_b_id, "parentId": root_org_id, "prevId": "missing-org"},
        )
        ensure_api_client_error(missing_prev_response, 10)

        cross_parent_prev_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/org/node/move",
            session_state=session_state,
            json_body={"nodeId": department_id, "parentId": company_b_id, "prevId": company_a_id},
        )
        ensure_api_client_error(cross_parent_prev_response, 10)

        cross_org_dept_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/org/node/move",
            session_state=session_state,
            json_body={"nodeId": department_id, "parentId": company_b_id, "prevId": ""},
        )
        ensure_api_client_error(cross_org_dept_response)

        move_response = self.ctx.client.request(
            "PUT",
            "/api/portal/v1/org/node/move",
            session_state=session_state,
            json_body={"nodeId": company_b_id, "parentId": root_org_id, "prevId": company_a_id},
        )
        ensure_http_status(move_response, 200)
        ensure_api_status(move_response.json())

        tree_response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/org/tree",
            session_state=session_state,
        )
        ensure_http_status(tree_response, 200)
        tree = ensure_api_status(tree_response.json())
        root_node = next((node for node in tree if node["data"]["id"] == root_org_id), None)
        if root_node is not None:
            top_level = root_node.get("children") or []
        else:
            top_level = tree
        filtered_ids = [node["data"]["id"] for node in top_level if node["data"]["id"] in {company_a_id, company_b_id}]
        self.assertEqual(filtered_ids, [company_a_id, company_b_id])

        company_b_detail_response = self.ctx.client.request(
            "GET",
            f"/api/portal/v1/org/node/{company_b_id}",
            session_state=session_state,
        )
        ensure_http_status(company_b_detail_response, 200)
        company_b_detail = ensure_api_status(company_b_detail_response.json())
        self.assertEqual(
            company_b_detail["parentId"],
            root_org_id,
            "parentId 传空时，服务端应将节点挂到租户默认根组织下",
        )

    def _create_perm_group(self, session_state, node_name: str, parent_id: str = "") -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/perm/group",
            session_state=session_state,
            json_body={"nodeName": node_name, "memo": "move flow perm group", "parentId": parent_id, "policyModel": 0},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())["id"]

    def _create_menu(self, session_state, app_id: str, name_cn: str, custom_id: str, parent_id: str = "") -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/resource/tree",
            session_state=session_state,
            json_body={
                "appId": app_id,
                "parentId": parent_id,
                "nameCn": name_cn,
                "resType": 0,
                "customId": custom_id,
                "clientType": 0,
                "icon": "icon-menu",
                "routeLink": f"/move/{custom_id}",
                "hidden": 0,
                "showLevel": 0,
                "forbidden": 0,
                "subPerms": [],
            },
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())["id"]

    def _create_org(self, session_state, parent_id: str, node_name: str, short_name: str, node_type: int) -> str:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/org/node",
            session_state=session_state,
            json_body={
                "nodeName": node_name,
                "shortName": short_name,
                "memo": "move flow org",
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
