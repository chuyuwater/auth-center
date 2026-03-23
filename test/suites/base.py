from __future__ import annotations

import traceback
import unittest

from common.context import TestContext


class BaseFlowTestCase(unittest.TestCase):
    ctx: TestContext

    @classmethod
    def setUpClass(cls) -> None:
        super().setUpClass()
        cls.ctx = TestContext()
        try:
            cls.ctx.login_all()
        except Exception:
            print("\n[auth-center-test] 初始化登录失败，完整异常如下：", flush=True)
            traceback.print_exc()
            raise
