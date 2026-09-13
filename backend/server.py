#!/usr/bin/env python3
"""Small dependency-free FireMind API for local development and fallback demos."""
from __future__ import annotations

import json
import re
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parent
CATALOG = json.loads((ROOT / "catalog.json").read_text(encoding="utf-8"))


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


def recommendation(movie: dict, query: str) -> dict:
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
    if not signals:
        signals.append(movie["mood"][0] if movie["mood"] else "your viewing mood")
    return {
        "title": movie["title"],
        "year": movie["year"],
        "genre": " · ".join(movie["genre"]),
        "runtime": movie["runtime"],
        "reason": f"Matches {', '.join(signals)} with a {movie['runtime']}-minute runtime and a strong audience rating.",
        "summary": movie["description"],
        "catalogId": movie["id"],
    }


class Handler(BaseHTTPRequestHandler):
    server_version = "FireMind/0.1"

    def _send(self, status: int, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self) -> None:  # noqa: N802
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.end_headers()

    def do_GET(self) -> None:  # noqa: N802
        if urlparse(self.path).path == "/api/health":
            self._send(200, {"ok": True, "service": "firemind", "catalogSize": len(CATALOG)})
            return
        self._send(404, {"error": "not_found"})

    def do_POST(self) -> None:  # noqa: N802
        path = urlparse(self.path).path
        try:
            length = int(self.headers.get("Content-Length", "0"))
            data = json.loads(self.rfile.read(length) or b"{}")
        except (ValueError, json.JSONDecodeError):
            self._send(400, {"error": "invalid_json"})
            return

        if path == "/api/recommend":
            query = data.get("query")
            if not isinstance(query, str) or not query.strip():
                self._send(400, {"error": "query_required"})
                return
            movies = rank(query, data.get("filters"))
            self._send(200, {"recommendations": [recommendation(movie, query) for movie in movies], "source": "local-catalog"})
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
    port = 8080
    print(f"FireMind API listening on http://127.0.0.1:{port}")
    ThreadingHTTPServer(("127.0.0.1", port), Handler).serve_forever()
