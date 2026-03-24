from __future__ import annotations

import re
from typing import Iterable

from common.assertions import ensure_api_status, ensure_http_status

HTTP_METHOD_MAP = {
    "GET": 0,
    "POST": 1,
    "PUT": 2,
    "DELETE": 3,
}


def collect_portal_perms_for_endpoints(ctx, session_state, endpoints: Iterable[tuple[str, str]]) -> list[dict]:
    tree_response = ctx.client.request(
        "GET",
        "/api/portal/v1/resource/tree",
        session_state=session_state,
        params={"appId": "portal", "withPerm": "true", "withApi": "true"},
    )
    ensure_http_status(tree_response, 200)
    tree = ensure_api_status(tree_response.json())

    perms: list[dict] = []
    _walk_perm_tree(tree, perms)

    matched: list[dict] = []
    seen_ids: set[str] = set()
    unmatched = set(endpoints)
    for perm in perms:
        for endpoint in endpoints:
            if any(matches_endpoint(api, endpoint[0], endpoint[1]) for api in perm["apis"]):
                unmatched.discard(endpoint)
                if perm["id"] not in seen_ids:
                    seen_ids.add(perm["id"])
                    matched.append(perm)
                break

    if unmatched:
        raise AssertionError(f"未在 portal 资源树中找到目标接口对应的权限点: {sorted(unmatched)}")
    return matched


def matches_endpoint(api: dict, method: str, path: str) -> bool:
    return api.get("apiMethod") == HTTP_METHOD_MAP[method.upper()] and ant_match(api.get("apiPath") or "", path)


def ant_match(pattern: str, path: str) -> bool:
    regex = []
    i = 0
    while i < len(pattern):
        ch = pattern[i]
        if ch == "*":
            if i + 1 < len(pattern) and pattern[i + 1] == "*":
                regex.append(".*")
                i += 2
            else:
                regex.append("[^/]*")
                i += 1
        elif ch == "?":
            regex.append("[^/]")
            i += 1
        else:
            regex.append(re.escape(ch))
            i += 1
    return re.fullmatch("".join(regex), path) is not None


def _walk_perm_tree(nodes: list[dict], perms: list[dict]) -> None:
    for node in nodes:
        data = node.get("data") or {}
        perm = data.get("perm") or {}
        if perm.get("id") and perm.get("permCode"):
            perms.append(
                {
                    "id": perm["id"],
                    "code": perm["permCode"],
                    "apis": [
                        {"apiMethod": api.get("apiMethod"), "apiPath": api.get("apiPath")}
                        for api in perm.get("apis") or []
                    ],
                }
            )
        _walk_perm_tree(node.get("children") or [], perms)
