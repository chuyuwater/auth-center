from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class SysDictFlowTestCase(BaseFlowTestCase):
    CLIENT_TYPE_FEAT_CODE = "CLIENT_TYPE"

    def test_sys_dict_crud_and_move_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        suffix = str(int(time.time() * 1000))

        group_id = self._get_group_id(admin_session, self.CLIENT_TYPE_FEAT_CODE)
        created_ids: list[str] = []
        try:
            missing_feat_code_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/list",
                session_state=admin_session,
            )
            ensure_api_client_error(missing_feat_code_response)

            missing_parent_id_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/sys/dict",
                session_state=admin_session,
                json_body={"parentId": "", "valueStr": f"auto_sys_dict_{suffix}", "valueCn": f"系统字典{suffix[-4:]}"},
            )
            ensure_api_client_error(missing_parent_id_response)

            dict_a = self._create_dict(admin_session, group_id, f"auto_sys_dict_a_{suffix}", f"系统字典A{suffix[-4:]}")
            created_ids.append(dict_a["id"])

            dict_b = self._create_dict(admin_session, group_id, f"auto_sys_dict_b_{suffix}", f"系统字典B{suffix[-4:]}")
            created_ids.append(dict_b["id"])

            duplicate_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/sys/dict",
                session_state=admin_session,
                json_body={
                    "parentId": group_id,
                    "valueStr": dict_a["valueStr"],
                    "valueCn": f"系统字典重复{suffix[-4:]}",
                    "forbidden": 0,
                },
            )
            ensure_api_client_error(duplicate_response)

            list_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/list",
                session_state=admin_session,
                params={"featCode": self.CLIENT_TYPE_FEAT_CODE},
            )
            ensure_http_status(list_response, 200)
            listed = ensure_api_status(list_response.json())
            filtered_values = [item["valueStr"] for item in listed if item["id"] in set(created_ids)]
            self.assertEqual(
                filtered_values,
                [dict_a["valueStr"], dict_b["valueStr"]],
                "新建字典项后，列表顺序应与创建顺序一致",
            )

            detail_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/sys/dict/{dict_a['id']}",
                session_state=admin_session,
            )
            ensure_http_status(detail_response, 200)
            detail = ensure_api_status(detail_response.json())
            self.assertEqual(detail["id"], dict_a["id"])
            self.assertEqual(detail["valueStr"], dict_a["valueStr"])

            update_response = self.ctx.client.request(
                "PUT",
                f"/api/portal/v1/sys/dict/{dict_a['id']}",
                session_state=admin_session,
                json_body={
                    "valueStr": f"auto_sys_dict_a_updated_{suffix}",
                    "valueCn": f"系统字典A更{suffix[-4:]}",
                    "forbidden": 1,
                },
            )
            ensure_http_status(update_response, 200)
            updated = ensure_api_status(update_response.json())
            self.assertEqual(updated["id"], dict_a["id"])
            self.assertEqual(updated["forbidden"], 1)

            children_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/children",
                session_state=admin_session,
                params={"featCode": self.CLIENT_TYPE_FEAT_CODE, "keyword": f"系统字典A更{suffix[-4:]}"},
            )
            ensure_http_status(children_response, 200)
            children = ensure_api_status(children_response.json())
            flattened_children = self._flatten_tree(children)
            self.assertTrue(
                any(item["id"] == dict_a["id"] for item in flattened_children),
                "children 查询未返回更新后的目标字典项",
            )

            invalid_move_response = self.ctx.client.request(
                "PUT",
                "/api/portal/v1/sys/dict/move",
                session_state=admin_session,
                json_body={"nodeId": dict_a["id"], "parentId": group_id, "prevId": dict_a["id"]},
            )
            ensure_api_client_error(invalid_move_response)

            move_response = self.ctx.client.request(
                "PUT",
                "/api/portal/v1/sys/dict/move",
                session_state=admin_session,
                json_body={"nodeId": dict_a["id"], "parentId": "ignored-parent", "prevId": dict_b["id"]},
            )
            ensure_http_status(move_response, 200)
            ensure_api_status(move_response.json())

            list_after_move_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/list",
                session_state=admin_session,
                params={"featCode": self.CLIENT_TYPE_FEAT_CODE},
            )
            ensure_http_status(list_after_move_response, 200)
            list_after_move = ensure_api_status(list_after_move_response.json())
            filtered_after_move = [item["id"] for item in list_after_move if item["id"] in set(created_ids)]
            self.assertEqual(
                filtered_after_move,
                [dict_b["id"], dict_a["id"]],
                "move 后字典项顺序应按 prevId 调整",
            )

            delete_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/sys/dict/delete",
                session_state=admin_session,
                params={"id": dict_b["id"]},
            )
            ensure_http_status(delete_response, 200)
            ensure_api_status(delete_response.json())
            created_ids.remove(dict_b["id"])

            list_after_delete_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/list",
                session_state=admin_session,
                params={"featCode": self.CLIENT_TYPE_FEAT_CODE},
            )
            ensure_http_status(list_after_delete_response, 200)
            list_after_delete = ensure_api_status(list_after_delete_response.json())
            self.assertFalse(
                any(item["id"] == dict_b["id"] for item in list_after_delete),
                "删除字典项后，列表中不应再出现被删除记录",
            )
            self.assertTrue(
                any(item["id"] == dict_a["id"] for item in list_after_delete),
                "删除兄弟节点后，其余测试字典项应继续存在",
            )

            children_after_delete_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/children",
                session_state=admin_session,
                params={"featCode": self.CLIENT_TYPE_FEAT_CODE, "keyword": f"系统字典B{suffix[-4:]}"},
            )
            ensure_http_status(children_after_delete_response, 200)
            children_after_delete = ensure_api_status(children_after_delete_response.json())
            self.assertFalse(
                any(item["id"] == dict_b["id"] for item in self._flatten_tree(children_after_delete)),
                "删除后 children 查询中不应再出现目标字典项",
            )

            missing_feat_children_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/sys/dict/children",
                session_state=admin_session,
            )
            ensure_api_client_error(missing_feat_children_response)
        finally:
            for dict_id in reversed(created_ids):
                delete_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/sys/dict/delete",
                    session_state=admin_session,
                    params={"id": dict_id},
                )
                ensure_http_status(delete_response, 200)
                ensure_api_status(delete_response.json())

    def _get_group_id(self, session_state, feat_code: str) -> str:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/sys/dict/list",
            session_state=session_state,
            params={"featCode": feat_code},
        )
        ensure_http_status(response, 200)
        items = ensure_api_status(response.json())
        self.assertTrue(items, f"初始化字典分组 {feat_code} 下没有任何数据")
        group_id = items[0]["parentId"]
        self.assertTrue(group_id, f"无法从字典分组 {feat_code} 推导 parentId")
        return group_id

    def _create_dict(self, session_state, parent_id: str, value_str: str, value_cn: str) -> dict:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/sys/dict",
            session_state=session_state,
            json_body={"parentId": parent_id, "valueStr": value_str, "valueCn": value_cn, "forbidden": 0},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    def _flatten_tree(self, nodes: list[dict]) -> list[dict]:
        flattened: list[dict] = []
        for node in nodes or []:
            data = node.get("data")
            if data is not None:
                flattened.append(data)
            flattened.extend(self._flatten_tree(node.get("children") or []))
        return flattened
