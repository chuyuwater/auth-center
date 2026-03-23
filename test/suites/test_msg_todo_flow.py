from __future__ import annotations

import time

from common.assertions import ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class MsgTodoFlowTestCase(BaseFlowTestCase):
    def test_msg_and_todo_query_scope_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        granted_session = self.ctx.session("granted_user")
        self.assertEqual(admin_session.tenant_id, granted_session.tenant_id, "该用例要求 super_admin 与 granted_user 在同一租户")
        org_ids = granted_session.account.org_ids
        self.assertGreaterEqual(len(org_ids), 2, "granted_user 至少需要配置两个组织用于切组织验权")

        granted_org_id = org_ids[0]
        forbidden_org_id = org_ids[1]
        suffix = str(int(time.time() * 1000))
        personal_msg_id = ""
        personal_todo_id = ""
        temp_user_granted_id = ""
        temp_user_forbidden_id = ""

        self.assertTrue(
            self._check_perm(granted_session, "msg:query", granted_org_id),
            f"granted_user 在组织 {granted_org_id} 下应具备消息查询权限",
        )
        self.assertFalse(
            self._check_perm(granted_session, "msg:query", forbidden_org_id),
            f"granted_user 在组织 {forbidden_org_id} 下不应具备消息查询权限",
        )
        self.assertTrue(
            self._check_perm(granted_session, "todo:query", granted_org_id),
            f"granted_user 在组织 {granted_org_id} 下应具备待办查询权限",
        )
        self.assertFalse(
            self._check_perm(granted_session, "todo:query", forbidden_org_id),
            f"granted_user 在组织 {forbidden_org_id} 下不应具备待办查询权限",
        )

        try:
            temp_user_granted_id = self._create_temp_user(
                admin_session, granted_org_id, suffix, "授权组织", "grant"
            )
            temp_user_forbidden_id = self._create_temp_user(
                admin_session, forbidden_org_id, suffix, "无权组织", "deny"
            )

            personal_msg_src_id = f"msg-personal-{suffix}"
            personal_todo_src_id = f"todo-personal-{suffix}"
            scoped_msg_granted_src_id = f"msg-granted-{suffix}"
            scoped_msg_forbidden_src_id = f"msg-forbidden-{suffix}"
            scoped_todo_granted_src_id = f"todo-granted-{suffix}"
            scoped_todo_forbidden_src_id = f"todo-forbidden-{suffix}"

            self._create_msg(
                admin_session,
                src_id=personal_msg_src_id,
                title=f"自动化本人消息{suffix[-6:]}",
                target_users=[granted_session.user_id],
            )
            self._create_todo(
                admin_session,
                src_id=personal_todo_src_id,
                title=f"自动化本人待办{suffix[-6:]}",
                target_users=[granted_session.user_id],
                initiator_id=admin_session.user_id,
                process_state=0,
            )

            self._create_msg(
                admin_session,
                src_id=scoped_msg_granted_src_id,
                title=f"自动化消息-授权组织-{suffix[-6:]}",
                target_users=[temp_user_granted_id],
            )
            self._create_msg(
                admin_session,
                src_id=scoped_msg_forbidden_src_id,
                title=f"自动化消息-无权组织-{suffix[-6:]}",
                target_users=[temp_user_forbidden_id],
            )
            self._create_todo(
                admin_session,
                src_id=scoped_todo_granted_src_id,
                title=f"自动化待办-授权组织-{suffix[-6:]}",
                target_users=[temp_user_granted_id],
                initiator_id=admin_session.user_id,
                process_state=0,
            )
            self._create_todo(
                admin_session,
                src_id=scoped_todo_forbidden_src_id,
                title=f"自动化待办-无权组织-{suffix[-6:]}",
                target_users=[temp_user_forbidden_id],
                initiator_id=admin_session.user_id,
                process_state=0,
            )

            my_msg_page = self._query_my_msgs(
                granted_session,
                keyword=f"自动化本人消息{suffix[-6:]}",
            )
            my_msg = self._pick_one(my_msg_page["list"], "msgTitle", f"自动化本人消息{suffix[-6:]}")
            personal_msg_id = my_msg["id"]
            self.assertEqual(my_msg["viewStatus"], 0)

            mark_msg_read_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/inbox/mark-as-read",
                session_state=granted_session,
                json_body={"msgIds": [personal_msg_id]},
            )
            ensure_http_status(mark_msg_read_response, 200)
            ensure_api_status(mark_msg_read_response.json())

            my_msg_after_read_page = self._query_my_msgs(
                granted_session,
                keyword=f"自动化本人消息{suffix[-6:]}",
            )
            my_msg_after_read = self._pick_one(
                my_msg_after_read_page["list"], "msgTitle", f"自动化本人消息{suffix[-6:]}"
            )
            self.assertEqual(my_msg_after_read["viewStatus"], 1)

            my_todo_page = self._query_my_todos(
                granted_session,
                keyword=f"自动化本人待办{suffix[-6:]}",
            )
            my_todo = self._pick_one(my_todo_page["list"], "todoTitle", f"自动化本人待办{suffix[-6:]}")
            personal_todo_id = my_todo["id"]
            self.assertEqual(my_todo["processState"], 0)
            self.assertEqual(my_todo["viewState"], 0)

            update_todo_response = self.ctx.client.request(
                "PUT",
                "/api/portal/v1/sdk/todo/update-state",
                session_state=admin_session,
                json_body={
                    "srcApp": "portal",
                    "srcId": personal_todo_src_id,
                    "userIds": [granted_session.user_id],
                    "processState": 2,
                    "urgeFlag": 1,
                },
            )
            ensure_http_status(update_todo_response, 200)
            ensure_api_status(update_todo_response.json())

            mark_todo_read_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/todo/mark-as-read",
                session_state=granted_session,
                json_body={"todoIds": [personal_todo_id]},
            )
            ensure_http_status(mark_todo_read_response, 200)
            ensure_api_status(mark_todo_read_response.json())

            my_todo_after_update_page = self._query_my_todos(
                granted_session,
                keyword=f"自动化本人待办{suffix[-6:]}",
            )
            my_todo_after_update = self._pick_one(
                my_todo_after_update_page["list"], "todoTitle", f"自动化本人待办{suffix[-6:]}"
            )
            self.assertEqual(my_todo_after_update["processState"], 2)
            self.assertEqual(my_todo_after_update["viewState"], 1)
            self.assertEqual(my_todo_after_update["urgeFlag"], 1)

            msg_page_granted_org = self._query_op_msgs(
                granted_session,
                org_id=granted_org_id,
                keyword=f"自动化消息-{suffix[-6:]}",
            )
            msg_titles_granted_org = {item["msgTitle"] for item in msg_page_granted_org["list"]}
            self.assertIn(f"自动化消息-授权组织-{suffix[-6:]}", msg_titles_granted_org)
            self.assertNotIn(f"自动化消息-无权组织-{suffix[-6:]}", msg_titles_granted_org)

            msg_page_forbidden_org = self._query_op_msgs(
                granted_session,
                org_id=forbidden_org_id,
                keyword=f"自动化消息-{suffix[-6:]}",
            )
            self.assertEqual(msg_page_forbidden_org["total"], 0)

            todo_page_granted_org = self._query_op_todos(
                granted_session,
                org_id=granted_org_id,
                keyword=f"自动化待办-{suffix[-6:]}",
            )
            todo_titles_granted_org = {item["todoTitle"] for item in todo_page_granted_org["list"]}
            self.assertIn(f"自动化待办-授权组织-{suffix[-6:]}", todo_titles_granted_org)
            self.assertNotIn(f"自动化待办-无权组织-{suffix[-6:]}", todo_titles_granted_org)

            todo_page_forbidden_org = self._query_op_todos(
                granted_session,
                org_id=forbidden_org_id,
                keyword=f"自动化待办-{suffix[-6:]}",
            )
            self.assertEqual(todo_page_forbidden_org["total"], 0)
        finally:
            if personal_msg_id:
                delete_msg_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/user/inbox/delete-batch",
                    session_state=granted_session,
                    json_body={"msgIds": [personal_msg_id]},
                )
                ensure_http_status(delete_msg_response, 200)
                ensure_api_status(delete_msg_response.json())

            if personal_todo_id:
                delete_todo_response = self.ctx.client.request(
                    "POST",
                    "/api/portal/v1/user/todo/delete-batch",
                    session_state=granted_session,
                    json_body={"todoIds": [personal_todo_id]},
                )
                ensure_http_status(delete_todo_response, 200)
                ensure_api_status(delete_todo_response.json())

            if temp_user_granted_id:
                self._delete_temp_user(admin_session, temp_user_granted_id)
            if temp_user_forbidden_id:
                self._delete_temp_user(admin_session, temp_user_forbidden_id)

    def _check_perm(self, session_state, perm_code: str, org_id: str) -> bool:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/client/perm-check",
            session_state=session_state,
            org_id=org_id,
            params={"permCode": perm_code},
        )
        ensure_http_status(response, 200)
        return bool(ensure_api_status(response.json()))

    def _create_temp_user(
        self, admin_session, node_id: str, suffix: str, label: str, account_tag: str
    ) -> str:
        create_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user",
            session_state=admin_session,
            json_body={
                "nodeId": node_id,
                "account": f"auto_{account_tag}_{suffix[-6:]}",
                "phone": self._build_phone("137", suffix + label),
                "realName": f"{label}用户{suffix[-4:]}",
                "email": f"auto_{account_tag}_{suffix[-6:]}@example.com",
                "avatar": "",
                "employeeType": 1,
            },
        )
        ensure_http_status(create_response, 200)
        user_id = ensure_api_status(create_response.json())
        self.assertTrue(user_id, f"{label}临时用户创建失败")
        return user_id

    def _delete_temp_user(self, admin_session, user_id: str) -> None:
        forbid_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/forbidden",
            session_state=admin_session,
            json_body={"userId": user_id, "forbidden": 1},
        )
        ensure_http_status(forbid_response, 200)
        ensure_api_status(forbid_response.json())

        delete_response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/delete",
            session_state=admin_session,
            params={"id": user_id},
        )
        ensure_http_status(delete_response, 200)
        ensure_api_status(delete_response.json())

    def _create_msg(self, admin_session, *, src_id: str, title: str, target_users: list[str]) -> None:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/sdk/msg",
            session_state=admin_session,
            json_body={
                "srcApp": "portal",
                "srcId": src_id,
                "title": title,
                "content": f"{title}内容",
                "link": f"/auto/msg/{src_id}",
                "type": 0,
                "targetUsers": target_users,
                "originJson": f'{{"srcId":"{src_id}"}}',
            },
        )
        ensure_http_status(response, 200)
        ensure_api_status(response.json())

    def _create_todo(
        self,
        admin_session,
        *,
        src_id: str,
        title: str,
        target_users: list[str],
        initiator_id: str,
        process_state: int,
    ) -> None:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/sdk/todo",
            session_state=admin_session,
            json_body={
                "srcApp": "portal",
                "srcId": src_id,
                "title": title,
                "content": f"{title}内容",
                "link": f"/auto/todo/{src_id}",
                "type": 0,
                "processState": process_state,
                "initiatorId": initiator_id,
                "targetUsers": target_users,
                "originJson": f'{{"srcId":"{src_id}"}}',
            },
        )
        ensure_http_status(response, 200)
        ensure_api_status(response.json())

    def _query_my_msgs(self, session_state, *, keyword: str) -> dict:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/user/inbox",
            session_state=session_state,
            params={"page": 1, "size": 20, "keyword": keyword},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    def _query_my_todos(self, session_state, *, keyword: str) -> dict:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/user/todo",
            session_state=session_state,
            params={"page": 1, "size": 20, "keyword": keyword, "scope": "TARGET_ME"},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    def _query_op_msgs(self, session_state, *, org_id: str, keyword: str) -> dict:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/msg",
            session_state=session_state,
            org_id=org_id,
            params={"page": 1, "size": 20, "keyword": keyword},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    def _query_op_todos(self, session_state, *, org_id: str, keyword: str) -> dict:
        response = self.ctx.client.request(
            "GET",
            "/api/portal/v1/todo",
            session_state=session_state,
            org_id=org_id,
            params={"page": 1, "size": 20, "keyword": keyword},
        )
        ensure_http_status(response, 200)
        return ensure_api_status(response.json())

    def _pick_one(self, items: list[dict], key: str, expected_value: str) -> dict:
        matched = [item for item in items if item.get(key) == expected_value]
        self.assertEqual(len(matched), 1, f"未唯一命中 {expected_value}, 实际结果: {items}")
        return matched[0]

    @staticmethod
    def _build_phone(prefix: str, seed: str) -> str:
        digits = "".join(ch for ch in seed if ch.isdigit())
        return prefix + digits[-8:]
