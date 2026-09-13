# FireMind

FireMind is an AI-ready viewing companion designed for Amazon Fire TV. It helps viewers move from an open-ended request to a short set of grounded recommendations, an explanation for every match, a spoiler-safe detail view, and a locally persisted watchlist.

## Current implementation

The first vertical slice is implemented in native Android/Kotlin for Fire OS. It includes a TV-first home screen, quick mood prompts, natural-language request input, deterministic recommendation ranking, details, explanation text, spoiler-safe summaries, local watchlist persistence, an About screen, local fallback behavior, and explicit D-pad focus styling.

The backend under `backend/` exposes the stable API contract. It uses the same curated catalog as the app and can optionally ask Amazon Bedrock to rank and explain only known candidates. If the SDK, credentials, model, network, or response validation fails, the backend automatically returns the deterministic local result instead.

## Requirements

- JDK 21 with `javac`.
- Android SDK Platform 35 and Build Tools 35.0.0.
- The project Gradle wrapper.
- A Fire TV device with developer access, or a compatible Android TV environment for UI validation.

The prepared sandbox toolkit is documented in `/home/ubuntu/firemind-tooling/FIREMIND_TOOLING.md`.

## Build the APK

```bash
source /home/ubuntu/firemind-tooling/env.sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd firemind
./gradlew clean assembleDebug
```

The verified debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Install on a Fire TV device

Enable ADB debugging and network access on the Fire TV device, identify its IP address, and connect from the development machine:

```bash
adb connect FIRE_TV_IP:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.firemind 1
adb logcat --pid="$(adb shell pidof -s com.firemind)"
```

The device workflow requires a real Fire TV or compatible target. No Fire TV simulator is present in this sandbox.

## Run the backend locally

```bash
cd backend
python3 server.py
```

The default server is `http://127.0.0.1:8080`. See [`docs/API_SPEC.md`](docs/API_SPEC.md) for endpoint contracts and curl examples.

## Optional Amazon Bedrock mode

Local mode requires no credentials. To enable the guarded Bedrock adapter, install the optional dependency and provide credentials through an AWS CLI profile, IAM role, or runtime environment. Do not put credentials in `.env` or source control.

```bash
cd backend
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
export AWS_REGION=us-east-1
export FIREMIND_BEDROCK_MODEL_ID=<approved-model-id>
python3 server.py
```

The adapter sends only the user request and known catalog candidates. It validates returned `catalogId` values against the local catalog before returning results. `GET /api/health` reports whether Bedrock mode is enabled. A deployed Fire TV demo must use an HTTPS backend endpoint; do not expose AWS credentials in the APK.

## Project structure

```text
app/                 Native Android/Kotlin Fire TV app
backend/             Dependency-free local API and optional Bedrock adapter
docs/API_SPEC.md     API contract
docs/ARCHITECTURE.md Architecture decisions
docs/TESTING.md      Verified and hardware-dependent checks
docs/DEMO_SCRIPT.md  Demo sequence
data/                Reserved for documented catalog exports
assets/              Reserved for legally usable visual assets
```

## TV UX verification checklist

Verify every interactive element with a remote or keyboard D-pad: UP, DOWN, LEFT, RIGHT, SELECT, and BACK. Confirm that focus is visible through both color and physical scale changes, that important content stays inside a safe-zone margin, and that text is legible at viewing distance. Human hardware verification remains required for overscan, ambient-light contrast, animation feel, and actual Fire TV performance.

## Security

No API keys are stored in the app. Do not commit `.env`, `local.properties`, credentials, keystores, or cloud credentials. The optional backend adapter fails closed to local mode when Bedrock is unavailable.

## License

MIT. See [`LICENSE`](LICENSE).
