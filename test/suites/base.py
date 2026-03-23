from __future__ import annotations

import unittest

from common.context import TestContext


class BaseFlowTestCase(unittest.TestCase):
    ctx: TestContext

    @classmethod
    def setUpClass(cls) -> None:
        super().setUpClass()
        cls.ctx = TestContext()
        cls.ctx.login_all()
