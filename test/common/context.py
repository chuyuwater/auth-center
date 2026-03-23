from __future__ import annotations

from dataclasses import dataclass, field

from common.client import AuthCenterClient, SessionState
from settings import ACCOUNTS


@dataclass
class TestContext:
    client: AuthCenterClient = field(default_factory=AuthCenterClient)
    sessions: dict[str, SessionState] = field(default_factory=dict)

    def login_all(self) -> None:
        for account_name in ACCOUNTS:
            account = self.client.build_account(account_name)
            self.sessions[account_name] = self.client.login(account)

    def session(self, account_name: str) -> SessionState:
        return self.sessions[account_name]
