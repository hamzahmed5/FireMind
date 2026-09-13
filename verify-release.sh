#!/usr/bin/env bash
set -euo pipefail
cd /home/ubuntu/firemind
python3 -m py_compile backend/server.py
python3 backend/server.py > /tmp/firemind-backend.log 2>&1 &
PID=$!
trap 'kill "$PID" 2>/dev/null || true' EXIT
sleep 1
curl -fsS http://127.0.0.1:8080/api/health > /tmp/firemind-health.json
curl -fsS -X POST http://127.0.0.1:8080/api/recommend -H 'Content-Type: application/json' -d '{"query":"mind-bending sci-fi under two hours"}' > /tmp/firemind-recommend.json
printf '{}' > /tmp/firemind-invalid-request.json
invalid_status=$(curl -s -o /tmp/firemind-invalid-response.json -w '%{http_code}' -X POST http://127.0.0.1:8080/api/recommend -H 'Content-Type: application/json' --data-binary @/tmp/firemind-invalid-request.json)
head -c 65537 /dev/zero | tr '\0' 'x' > /tmp/firemind-large-request.json
large_status=$(curl -s -o /tmp/firemind-large-response.json -w '%{http_code}' -X POST http://127.0.0.1:8080/api/recommend --data-binary @/tmp/firemind-large-request.json)
printf 'health='; cat /tmp/firemind-health.json; printf '\n'
printf 'recommend='; grep -o '"source"[^}]*' /tmp/firemind-recommend.json | head -1; printf '\n'
printf 'invalid_status=%s\n' "$invalid_status"
printf 'large_status=%s\n' "$large_status"
[ "$invalid_status" = 400 ]
[ "$large_status" = 413 ]
source /home/ubuntu/firemind-tooling/env.sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew clean test assembleDebug
for f in README.md LICENSE backend/.env.example docs/API_SPEC.md docs/DEVPOST_SUBMISSION.md docs/PRODUCT_FEEDBACK.md docs/RELEASE_CHECKLIST.md app/build/outputs/apk/debug/app-debug.apk; do test -s "$f"; done
printf '%s\n' '--- final APK ---'
ls -lh app/build/outputs/apk/debug/app-debug.apk
