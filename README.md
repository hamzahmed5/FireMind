# FireMind

FireMind is an AI-ready viewing companion designed for Amazon Fire TV. It helps viewers move from an open-ended request to a short set of grounded recommendations, an explanation for every match, a spoiler-safe detail view, and a locally persisted watchlist.

## Current implementation

The first vertical slice is implemented in native Android/Kotlin for Fire OS. It includes a TV-first home screen, quick mood prompts, natural-language request input, deterministic recommendation ranking, details, explanation text, spoiler-safe summaries, local watchlist persistence, an About screen, loading-safe synchronous local fallback behavior, and explicit D-pad focus styling.

The backend contract is implemented as a dependency-free Python service under `backend/`. The Android app currently uses the same local catalog and ranking logic so the core demo remains reliable without cloud credentials. Amazon Bedrock integration is intentionally not claimed until AWS credentials and a deployed HTTPS endpoint are available.

## Requirements

- JDK 21 with `javac`.
- Android SDK Platform 35 and Build Tools 35.0.0.
- Gradle 9.7.1 or the project wrapper once generated.
- A Fire TV device with developer access, or a compatible Android TV environment for UI validation.

The prepared sandbox toolkit is documented in `/home/ubuntu/firemind-tooling/FIREMIND_TOOLING.md`.

## Build the APK

```bash
source /home/ubuntu/firemind-tooling/env.sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd firemind
gradle clean assembleDebug
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

## Run the backend

```bash
cd backend
python3 server.py
```

See [`docs/API_SPEC.md`](docs/API_SPEC.md) for endpoint contracts and curl examples. The backend does not require credentials and does not claim an AWS runtime integration.

## Project structure

```text
app/                 Native Android/Kotlin Fire TV app
backend/             Dependency-free local API and curated catalog
docs/API_SPEC.md     API contract
data/                Reserved for documented catalog exports
assets/              Reserved for legally usable visual assets
```

## TV UX verification checklist

Verify every interactive element with a remote or keyboard D-pad: UP, DOWN, LEFT, RIGHT, SELECT, and BACK. Confirm that focus is visible through both color and physical scale changes, that important content stays inside a safe-zone margin, and that text is legible at viewing distance. Human hardware verification remains required for overscan, ambient-light contrast, animation feel, and actual Fire TV performance.

## Security

No API keys are stored in the app. Do not commit `.env`, `local.properties`, credentials, keystores, or cloud credentials. A future cloud adapter must keep provider credentials on the backend and use HTTPS from the Fire TV app.

## License

MIT. See [`LICENSE`](LICENSE).
