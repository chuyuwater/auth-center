from __future__ import annotations

from suites.base import BaseFlowTestCase


class AuthFlowTestCase(BaseFlowTestCase):
    def test_all_configured_accounts_can_login(self) -> None:
        for account_name, session_state in self.ctx.sessions.items():
            self.assertTrue(session_state.token, f"{account_name} token 为空")
            self.assertTrue(session_state.user_id, f"{account_name} userId 为空")
            self.assertTrue(session_state.tenant_id, f"{account_name} tenantId 为空")
