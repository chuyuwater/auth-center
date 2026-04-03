from __future__ import annotations

import time

from common.assertions import ensure_api_client_error, ensure_api_status, ensure_http_status
from suites.base import BaseFlowTestCase


class UserCustomStyleFlowTestCase(BaseFlowTestCase):
    USER_CUSTOM_STYLE_FEAT_CODE = "USER_CUSTOM_STYLE"

    def test_user_custom_style_crud_flow(self) -> None:
        admin_session = self.ctx.session("super_admin")
        user_session = self.ctx.session("granted_user")
        suffix = str(int(time.time() * 1000))

        group_id = self._get_group_id(admin_session, self.USER_CUSTOM_STYLE_FEAT_CODE)
        item_code = f"AUTO_STYLE_{suffix[-10:]}"
        first_setting = f"option_{suffix[-4:]}_a"
        second_setting = f"option_{suffix[-4:]}_b"
        created_dict_ids: list[str] = []

        try:
            item_dict = self._create_dict(admin_session, group_id, item_code, f"自动页面风格{suffix[-4:]}")
            created_dict_ids.append(item_dict["id"])

            first_setting_dict = self._create_dict(
                admin_session,
                item_dict["id"],
                first_setting,
                f"风格选项A{suffix[-4:]}",
            )
            created_dict_ids.append(first_setting_dict["id"])

            second_setting_dict = self._create_dict(
                admin_session,
                item_dict["id"],
                second_setting,
                f"风格选项B{suffix[-4:]}",
            )
            created_dict_ids.append(second_setting_dict["id"])

            self._delete_style_quietly(user_session, item_code)

            list_empty_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                params={"item": item_code},
            )
            ensure_http_status(list_empty_response, 200)
            self.assertEqual(ensure_api_status(list_empty_response.json()), [])

            detail_empty_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/custom-style/{item_code}",
                session_state=user_session,
            )
            ensure_http_status(detail_empty_response, 200)
            self.assertIsNone(ensure_api_status(detail_empty_response.json()))

            invalid_setting_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                json_body={"item": item_code, "setting": "not_allowed"},
            )
            ensure_api_client_error(invalid_setting_response)

            create_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                json_body={"item": item_code, "setting": first_setting},
            )
            ensure_http_status(create_response, 200)
            created = ensure_api_status(create_response.json())
            self.assertEqual(created["item"], item_code)
            self.assertEqual(created["setting"], first_setting)
            self.assertEqual(created["userId"], user_session.user_id)
            self.assertTrue(created["id"])

            detail_after_create_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/custom-style/{item_code}",
                session_state=user_session,
            )
            ensure_http_status(detail_after_create_response, 200)
            detail_after_create = ensure_api_status(detail_after_create_response.json())
            self.assertIsNotNone(detail_after_create)
            self.assertEqual(detail_after_create["item"], item_code)
            self.assertEqual(detail_after_create["setting"], first_setting)

            list_after_create_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                params={"item": item_code},
            )
            ensure_http_status(list_after_create_response, 200)
            listed_after_create = ensure_api_status(list_after_create_response.json())
            self.assertEqual(len(listed_after_create), 1)
            self.assertEqual(listed_after_create[0]["setting"], first_setting)

            update_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                json_body={"item": item_code, "setting": second_setting},
            )
            ensure_http_status(update_response, 200)
            updated = ensure_api_status(update_response.json())
            self.assertEqual(updated["item"], item_code)
            self.assertEqual(updated["setting"], second_setting)

            detail_after_update_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/custom-style/{item_code}",
                session_state=user_session,
            )
            ensure_http_status(detail_after_update_response, 200)
            detail_after_update = ensure_api_status(detail_after_update_response.json())
            self.assertIsNotNone(detail_after_update)
            self.assertEqual(detail_after_update["setting"], second_setting)

            list_all_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
            )
            ensure_http_status(list_all_response, 200)
            listed_all = ensure_api_status(list_all_response.json())
            self.assertTrue(
                any(item["item"] == item_code and item["setting"] == second_setting for item in listed_all),
                "全量列表中未返回刚刚更新后的自定义风格",
            )

            delete_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/custom-style/delete",
                session_state=user_session,
                params={"item": item_code},
            )
            ensure_http_status(delete_response, 200)
            ensure_api_status(delete_response.json())

            detail_after_delete_response = self.ctx.client.request(
                "GET",
                f"/api/portal/v1/user/custom-style/{item_code}",
                session_state=user_session,
            )
            ensure_http_status(detail_after_delete_response, 200)
            self.assertIsNone(ensure_api_status(detail_after_delete_response.json()))

            list_after_delete_response = self.ctx.client.request(
                "GET",
                "/api/portal/v1/user/custom-style",
                session_state=user_session,
                params={"item": item_code},
            )
            ensure_http_status(list_after_delete_response, 200)
            self.assertEqual(ensure_api_status(list_after_delete_response.json()), [])

            delete_again_response = self.ctx.client.request(
                "POST",
                "/api/portal/v1/user/custom-style/delete",
                session_state=user_session,
                params={"item": item_code},
            )
            ensure_http_status(delete_again_response, 200)
            ensure_api_status(delete_again_response.json())
        finally:
            self._delete_style_quietly(user_session, item_code)
            for dict_id in reversed(created_dict_ids):
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

    def _delete_style_quietly(self, session_state, item_code: str) -> None:
        response = self.ctx.client.request(
            "POST",
            "/api/portal/v1/user/custom-style/delete",
            session_state=session_state,
            params={"item": item_code},
        )
        ensure_http_status(response, 200)
        ensure_api_status(response.json())
