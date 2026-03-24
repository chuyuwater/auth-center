from __future__ import annotations

from dataclasses import dataclass, field

from common.client import AccountConfig, AuthCenterClient, SessionState
from settings import ACCOUNTS

_SHARED_CONTEXT: "TestContext | None" = None


@dataclass
class TestContext:
    client: AuthCenterClient = field(default_factory=AuthCenterClient)
    sessions: dict[str, SessionState] = field(default_factory=dict)
    initialized: bool = False
    closed: bool = False

    def login_all(self) -> None:
        if self.initialized:
            return
        for account_name in ACCOUNTS:
            account = self.client.build_account(account_name)
            try:
                self.sessions[account_name] = self.client.login(account)
            except Exception as exc:
                raise RuntimeError(f"初始化登录失败，账号={account_name}: {exc}") from exc
        self.initialized = True

    def session(self, account_name: str) -> SessionState:
        return self.sessions[account_name]

    def login_custom(self, account: AccountConfig, alias: str | None = None) -> SessionState:
        try:
            session = self.client.login(account)
        except Exception as exc:
            raise RuntimeError(f"自定义账号登录失败，账号={alias or account.name}: {exc}") from exc
        self.sessions[alias or account.name] = session
        return session

    def logout(self, account_name: str) -> None:
        self.client.logout(self.sessions[account_name])

    def relogin(self, account_name: str) -> SessionState:
        account = self.sessions[account_name].account
        try:
            session = self.client.login(account)
        except Exception as exc:
            raise RuntimeError(f"重新登录失败，账号={account_name}: {exc}") from exc
        self.sessions[account_name] = session
        return session

    def shutdown(self) -> None:
        if self.closed:
            return
        for account_name in reversed(list(self.sessions.keys())):
            session = self.sessions.get(account_name)
            if session is None:
                continue
            self.client.logout_quietly(session)
        self.closed = True


def shutdown_shared_context() -> None:
    global _SHARED_CONTEXT
    if _SHARED_CONTEXT is not None:
        _SHARED_CONTEXT.shutdown()


def get_shared_context() -> TestContext:
    global _SHARED_CONTEXT
    if _SHARED_CONTEXT is None:
        _SHARED_CONTEXT = TestContext()
        _SHARED_CONTEXT.login_all()
    return _SHARED_CONTEXT
