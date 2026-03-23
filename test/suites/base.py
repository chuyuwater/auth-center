from __future__ import annotations

import traceback
import unittest

from common.context import TestContext, get_shared_context


class BaseFlowTestCase(unittest.TestCase):
    ctx: TestContext

    @classmethod
    def setUpClass(cls) -> None:
        super().setUpClass()
        try:
            cls.ctx = get_shared_context()
        except Exception:
            print("\n[auth-center-test] 初始化登录失败，完整异常如下：", flush=True)
            traceback.print_exc()
            raise
