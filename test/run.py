from __future__ import annotations

import sys
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
    raise SystemExit(main())
