from __future__ import annotations

from common.assertions import ensure_api_status, ensure_http_status


def delete_user_safely(ctx, session_state, user_id: str) -> None:
    forbid_response = ctx.client.request(
        "POST",
        "/api/portal/v1/user/forbidden",
        session_state=session_state,
        json_body={"userId": user_id, "forbidden": 1},
    )
    ensure_http_status(forbid_response, 200)
    ensure_api_status(forbid_response.json())

    delete_response = ctx.client.request(
        "POST",
        "/api/portal/v1/user/delete",
        session_state=session_state,
        params={"id": user_id},
    )
    ensure_http_status(delete_response, 200)
    ensure_api_status(delete_response.json())


def delete_org_tree_safely(ctx, session_state, root_org_id: str) -> None:
    tree_response = ctx.client.request(
        "GET",
        "/api/portal/v1/org/tree",
        session_state=session_state,
    )
    ensure_http_status(tree_response, 200)
    tree = ensure_api_status(tree_response.json())
    pending_ids = _collect_org_ids_for_delete(tree, root_org_id)
    for node_id in pending_ids:
        delete_response = ctx.client.request(
            "POST",
            "/api/portal/v1/org/node/delete",
            session_state=session_state,
            params={"id": node_id},
        )
        ensure_http_status(delete_response, 200)
        ensure_api_status(delete_response.json())


def _collect_org_ids_for_delete(nodes: list[dict], root_org_id: str) -> list[str]:
    ids: list[str] = []
    for node in nodes:
        ids.extend(_collect_org_ids_for_delete(node.get("children") or [], root_org_id))
        data = node.get("data") or {}
        node_id = data.get("id")
        if node_id and node_id != root_org_id:
            ids.append(node_id)
    return ids
