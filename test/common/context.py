from __future__ import annotations

from dataclasses import dataclass, field

from common.client import AccountConfig, AuthCenterClient, SessionState
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

    def login_custom(self, account: AccountConfig, alias: str | None = None) -> SessionState:
        session = self.client.login(account)
        self.sessions[alias or account.name] = session
        return session

    def logout(self, account_name: str) -> None:
        self.client.logout(self.sessions[account_name])

    def relogin(self, account_name: str) -> SessionState:
        account = self.sessions[account_name].account
        session = self.client.login(account)
        self.sessions[account_name] = session
        return session
