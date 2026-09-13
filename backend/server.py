#!/usr/bin/env python3
"""FireMind API with grounded local recommendations and optional Bedrock ranking."""
from __future__ import annotations

import json
import os
import re
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parent
CATALOG = json.loads((ROOT / "catalog.json").read_text(encoding="utf-8"))
MAX_BODY_BYTES = 64 * 1024
ALLOWED_ORIGIN = os.getenv("FIREMIND_ALLOWED_ORIGIN", "*")
BEDROCK_MODEL_ID = os.getenv("FIREMIND_BEDROCK_MODEL_ID", "")

try:
    import boto3  # type: ignore
except ImportError:  # Optional dependency; local mode must work without it.
    boto3 = None


def bedrock_enabled() -> bool:
    return bool(BEDROCK_MODEL_ID and boto3 is not None)


def rank(query: str, filters: dict | None = None) -> list[dict]:
    normalized = query.lower()
    filters = filters or {}
    runtime_max = filters.get("runtimeMax")
    if not runtime_max:
        match = re.search(r"(?:under|less than)\s+(\d+)\s*(?:minutes?|min)?", normalized)
        runtime_max = int(match.group(1)) if match else None
    if not runtime_max and ("two hour" in normalized or "2 hour" in normalized):
        runtime_max = 120
    if not runtime_max and "short" in normalized:
        runtime_max = 105

    scored: list[tuple[int, float, dict]] = []
    for movie in CATALOG:
        score = 0
        if runtime_max is None or movie["runtime"] <= runtime_max:
            score += 3
        if "family" in normalized and movie["familyFriendly"]:
            score += 6
        for mood in movie["mood"]:
            if mood in normalized:
                score += 5
        for genre in movie["genre"]:
            if genre.lower() in normalized:
                score += 5
        if "comedy" in normalized or "funny" in normalized:
            score += 5 if "Comedy" in movie["genre"] else 0
        scored.append((score, movie["rating"], movie))

    scored.sort(key=lambda item: (item[0], item[1]), reverse=True)
    results = [movie for score, _, movie in scored if score > 0][:5]
    return results or [movie for _, _, movie in scored[:3]]


def recommendation(movie: dict, query: str, reason: str | None = None, summary: str | None = None) -> dict:
    signals = []
    normalized = query.lower()
    if "sci-fi" in normalized and "Sci-Fi" in movie["genre"]:
        signals.append("sci-fi")
    if ("funny" in normalized or "comedy" in normalized) and "Comedy" in movie["genre"]:
        signals.append("comedy")
    if "family" in normalized and movie["familyFriendly"]:
        signals.append("family-friendly")
    if any(token in normalized for token in ("under", "short", "hour")) and movie["runtime"] <= 120:
        signals.append("your time window")
    fallback_reason = f"Matches {', '.join(signals or [movie['mood'][0]])} with a {movie['runtime']}-minute runtime and a strong audience rating."
    return {
        "title": movie["title"],
        "year": movie["year"],
        "genre": " · ".join(movie["genre"]),
        "runtime": movie["runtime"],
        "reason": reason or fallback_reason,
        "summary": summary or movie["description"],
        "catalogId": movie["id"],
    }


def bedrock_recommend(query: str, candidates: list[dict]) -> list[dict] | None:
    """Ask Bedrock to rank known candidates; return None for safe local fallback."""
    if not bedrock_enabled():
        return None
    candidate_payload = [{
        "catalogId": item["id"], "title": item["title"], "year": item["year"],
        "genre": item["genre"], "runtime": item["runtime"], "description": item["description"]
    } for item in candidates]
    prompt = (
        "Rank only these known catalog candidates for the request. Do not invent titles or metadata. "
        "Return JSON only with key recommendations, an array of objects containing catalogId, reason, and summary. "
        f"Request: {query}\nCandidates: {json.dumps(candidate_payload, ensure_ascii=False)}"
    )
    try:
        client = boto3.client("bedrock-runtime", region_name=os.getenv("AWS_REGION", "us-east-1"))
        response = client.converse(
            modelId=BEDROCK_MODEL_ID,
            messages=[{"role": "user", "content": [{"text": prompt}]}],
            inferenceConfig={"maxTokens": 900, "temperature": 0.2},
        )
        raw = response["output"]["message"]["content"][0]["text"].strip()
        raw = re.sub(r"^```(?:json)?\s*|\s*```$", "", raw).strip()
        parsed = json.loads(raw)
        allowed = {item["id"]: item for item in candidates}
        output = []
        for item in parsed.get("recommendations", []):
            movie = allowed.get(item.get("catalogId"))
            if not movie or not isinstance(item.get("reason"), str) or not isinstance(item.get("summary"), str):
                continue
            output.append(recommendation(movie, query, item["reason"], item["summary"]))
        return output[:5] or None
    except Exception as exc:  # Model/network/credential errors must not break the demo.
        print(f"Bedrock unavailable; using local fallback: {type(exc).__name__}")
        return None


class Handler(BaseHTTPRequestHandler):
    server_version = "FireMind/0.2"

    def _send(self, status: int, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", ALLOWED_ORIGIN)
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self) -> None:  # noqa: N802
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", ALLOWED_ORIGIN)
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.end_headers()

    def do_GET(self) -> None:  # noqa: N802
        if urlparse(self.path).path == "/api/health":
            self._send(200, {"ok": True, "service": "firemind", "catalogSize": len(CATALOG), "aiEnabled": bedrock_enabled()})
            return
        self._send(404, {"error": "not_found"})

    def do_POST(self) -> None:  # noqa: N802
        path = urlparse(self.path).path
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length > MAX_BODY_BYTES:
                self._send(413, {"error": "request_too_large"})
                return
            data = json.loads(self.rfile.read(length) or b"{}")
        except (ValueError, json.JSONDecodeError):
            self._send(400, {"error": "invalid_json"})
            return

        if path == "/api/recommend":
            query = data.get("query")
            if not isinstance(query, str) or not query.strip():
                self._send(400, {"error": "query_required"})
                return
            candidates = rank(query, data.get("filters"))
            ai_results = bedrock_recommend(query, candidates)
            results = ai_results or [recommendation(movie, query) for movie in candidates]
            self._send(200, {"recommendations": results, "source": "amazon-bedrock" if ai_results else "local-catalog"})
            return

        if path == "/api/summarize":
            movie_id = data.get("catalogId")
            movie = next((item for item in CATALOG if item["id"] == movie_id), None)
            if not movie:
                self._send(404, {"error": "title_not_found"})
                return
            self._send(200, {"catalogId": movie_id, "summary": movie["description"], "spoilerSafe": True})
            return

        if path == "/api/similar":
            movie_id = data.get("catalogId")
            movie = next((item for item in CATALOG if item["id"] == movie_id), None)
            if not movie:
                self._send(404, {"error": "title_not_found"})
                return
            candidates = [item for item in CATALOG if item["id"] != movie_id and set(item["genre"]) & set(movie["genre"])]
            self._send(200, {"recommendations": [recommendation(item, " ".join(movie["genre"])) for item in candidates[:3]], "source": "local-catalog"})
            return

        self._send(404, {"error": "not_found"})

    def log_message(self, format: str, *args: object) -> None:
        print(format % args)


if __name__ == "__main__":
    port = int(os.getenv("FIREMIND_PORT", "8080"))
    print(f"FireMind API listening on http://127.0.0.1:{port}")
    ThreadingHTTPServer((os.getenv("FIREMIND_HOST", "127.0.0.1"), port), Handler).serve_forever()
