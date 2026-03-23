from __future__ import annotations

import sys
import traceback
import unittest
from pathlib import Path


def main() -> int:
    base_dir = Path(__file__).resolve().parent
    suite_dir = base_dir / "suites"
    loader = unittest.defaultTestLoader
    suite = loader.discover(str(suite_dir), pattern="test_*.py")
    result = unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception:
        print("\n[auth-center-test] 运行异常，完整 traceback 如下：", file=sys.stderr, flush=True)
        traceback.print_exc()
        raise
