# FireMind API Specification

The FireMind backend exposes a small JSON API. The default implementation is dependency-free and uses a curated local catalog. If `FIREMIND_BEDROCK_MODEL_ID` is configured and the optional AWS SDK is installed, the backend can ask Amazon Bedrock to rank and explain only known candidates. Any credential, network, model, or validation failure returns the deterministic local result instead.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Verify service availability, catalog size, and whether Bedrock mode is enabled. |
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
      "summary": "A solitary lunar worker begins to question the reality of his final weeks on the moon.",
      "catalogId": "moon"
    }
  ],
  "source": "local-catalog"
}
```

`source` is `amazon-bedrock` only after the backend validates the model response against the local catalog. Otherwise it is `local-catalog`.

## Status and errors

The API returns `400` for malformed JSON or missing `query`, `404` for unknown routes or titles, `413` for request bodies over 64 KiB, and `200` for valid requests. CORS is configurable with `FIREMIND_ALLOWED_ORIGIN`; the default wildcard is suitable only for local development.

## Local run

```bash
cd backend
python3 server.py
curl http://127.0.0.1:8080/api/health
curl -X POST http://127.0.0.1:8080/api/recommend \
  -H 'Content-Type: application/json' \
  -d '{"query":"funny family movie under two hours"}'
```

## Bedrock configuration

```bash
pip install -r requirements.txt
export AWS_REGION=us-east-1
export FIREMIND_BEDROCK_MODEL_ID=<approved-model-id>
python3 server.py
```

Credentials must come from an AWS profile, IAM role, or runtime secret store. Never place them in the Android app or source control.
