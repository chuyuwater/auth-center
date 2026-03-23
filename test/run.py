from __future__ import annotations

import sys
import traceback
import unittest
from pathlib import Path


class VerboseTextTestResult(unittest.TextTestResult):
    def addError(self, test, err):
        super().addError(test, err)
        self._print_immediate_detail("ERROR", test, err)

    def addFailure(self, test, err):
        super().addFailure(test, err)
        self._print_immediate_detail("FAIL", test, err)

    def _print_immediate_detail(self, label: str, test, err) -> None:
        detail = self._exc_info_to_string(err, test)
        self.stream.writeln("")
        self.stream.writeln(f"[auth-center-test] {label}: {test.id()}")
        self.stream.writeln(detail)
        ctx = getattr(test.__class__, "ctx", None)
        client = getattr(ctx, "client", None)
        formatter = getattr(client, "format_recent_exchanges", None)
        if callable(formatter):
            self.stream.writeln("[auth-center-test] recent_exchanges:")
            self.stream.writeln(formatter())
        self.stream.flush()


def main() -> int:
    base_dir = Path(__file__).resolve().parent
    suite_dir = base_dir / "suites"
    loader = unittest.defaultTestLoader
    suite = loader.discover(str(suite_dir), pattern="test_*.py")
    runner = unittest.TextTestRunner(verbosity=2, resultclass=VerboseTextTestResult, buffer=False)
    result = runner.run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception:
        print("\n[auth-center-test] 运行异常，完整 traceback 如下：", file=sys.stderr, flush=True)
        traceback.print_exc()
        raise
