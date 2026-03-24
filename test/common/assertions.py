from __future__ import annotations

from typing import Any


class ApiAssertionError(AssertionError):
    pass


def ensure_http_status(response, expected_status: int) -> None:
    if response.status_code != expected_status:
        request = response.request
        raise ApiAssertionError(
            f"unexpected http status: {response.status_code}, "
            f"expected: {expected_status}, "
            f"method: {request.method}, url: {request.url}, "
            f"request_body: {request.body}, response_body: {response.text}"
        )


def ensure_http_client_error(response) -> None:
    if response.status_code < 400 or response.status_code >= 500:
        request = response.request
        raise ApiAssertionError(
            f"unexpected http status: {response.status_code}, "
            "expected: 4xx client error, "
            f"method: {request.method}, url: {request.url}, "
            f"request_body: {request.body}, response_body: {response.text}"
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
        request = response.request
        raise ApiAssertionError(
            f"unexpected api status: {body.get('status')}, expected: {biz_status}, "
            f"method: {request.method}, url: {request.url}, request_body: {request.body}, body: {body}"
        )
    return body


def ensure_api_client_error(response, biz_status: int | None = None) -> dict[str, Any]:
    ensure_http_client_error(response)
    body = response.json()
    if biz_status is not None and body.get("status") != biz_status:
        request = response.request
        raise ApiAssertionError(
            f"unexpected api status: {body.get('status')}, expected: {biz_status}, "
            f"method: {request.method}, url: {request.url}, request_body: {request.body}, body: {body}"
        )
    return body
