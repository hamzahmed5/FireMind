# FireMind API Specification

The FireMind backend exposes a small JSON API. The current implementation is dependency-free and uses a curated local catalog. A future Amazon Bedrock adapter may replace the ranking/explanation step without changing the response contract.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Verify service availability and catalog size. |
| POST | `/api/recommend` | Return three to five grounded recommendations for a natural-language request. |
| POST | `/api/summarize` | Return a spoiler-safe summary for a catalog title. |
| POST | `/api/similar` | Return titles sharing one or more genres with a selected title. |

## Recommendation request

```json
{
  "query": "I want a mind-bending sci-fi movie under two hours",
  "filters": {"runtimeMax": 120}
}
```

## Recommendation response

```json
{
  "recommendations": [
    {
      "title": "Moon",
      "year": 2009,
      "genre": "Sci-Fi · Mystery",
      "runtime": 97,
      "reason": "Matches sci-fi, your time window with a 97-minute runtime and a strong audience rating.",
      "summary": "...",
      "catalogId": "moon"
    }
  ],
  "source": "local-catalog"
}
```

The API returns `400` for malformed JSON or missing `query`, `404` for unknown routes or titles, and `200` for valid requests. The Android app currently contains the same deterministic fallback logic locally, so a backend outage does not freeze or empty the demo flow.

## Local run

```bash
cd backend
python3 server.py
curl http://127.0.0.1:8080/api/health
curl -X POST http://127.0.0.1:8080/api/recommend \
  -H 'Content-Type: application/json' \
  -d '{"query":"funny family movie under two hours"}'
```
