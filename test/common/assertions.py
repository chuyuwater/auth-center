from __future__ import annotations

from typing import Any


class ApiAssertionError(AssertionError):
    pass


def ensure_http_status(response, expected_status: int) -> None:
    if response.status_code != expected_status:
        raise ApiAssertionError(
            f"unexpected http status: {response.status_code}, "
            f"expected: {expected_status}, body: {response.text}"
        )


def ensure_api_status(body: dict[str, Any], expected_status: int = 0) -> Any:
    actual_status = body.get("status")
    if actual_status != expected_status:
        raise ApiAssertionError(
            f"unexpected api status: {actual_status}, expected: {expected_status}, body: {body}"
        )
    return body.get("data")


def ensure_api_error(response, http_status: int, biz_status: int | None = None) -> dict[str, Any]:
    ensure_http_status(response, http_status)
    body = response.json()
    if biz_status is not None and body.get("status") != biz_status:
        raise ApiAssertionError(
            f"unexpected api status: {body.get('status')}, expected: {biz_status}, body: {body}"
        )
    return body
