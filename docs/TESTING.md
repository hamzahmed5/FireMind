# FireMind Testing

## Verified in the development sandbox

| Area | Result | Evidence |
|---|---|---|
| Android build | Passed | `./gradlew clean test assembleDebug` |
| APK metadata | Passed | `aapt2 dump badging` reports `com.firemind`, target SDK 35, lean-back launcher |
| Backend syntax | Passed | `python3 -m py_compile backend/server.py` |
| Backend health | Passed | `GET /api/health` returned HTTP 200 |
| Recommendation contract | Passed | `POST /api/recommend` returned grounded catalog results |
| Summary contract | Passed | `POST /api/summarize` returned spoiler-safe summary |
| Validation | Passed | Missing query returned HTTP 400; oversized body returned HTTP 413 |
| Secret filename audit | Passed | No credential, keystore, or certificate files in project |
| TV runtime install | Passed | APK installed on Android TV 10 x86 emulator via ADB |
| TV runtime launch | Passed | `com.firemind/.MainActivity` resumed successfully |
| TV rendering | Passed | Home, Recommendations, Details, and Watchlist rendered at 1920×1080 |
| D-pad-style navigation | Passed | Home → Funny → Recommendations → Details navigated through emulator key events |
| Watchlist persistence | Passed | Add changed to Remove and Watchlist displayed Sing Street |
| Screenshots | Passed | Runtime PNG captures stored under `media/` |
| Demo recording | Passed | H.264 MP4 captures stored under `media/` |

## Hardware-dependent checks

The emulator is a compatible Android TV validation target, not a physical Fire TV. The following checks still require Fire TV hardware: physical remote feel, overscan, hardware performance, Fire OS-specific behavior, and network reachability from an actual Fire TV device.

## AI-dependent checks

AWS credentials and an approved Bedrock model are not configured. Local fallback mode is fully tested. Live Bedrock execution, HTTPS deployment, and Fire TV-to-backend integration remain pending until AWS access is supplied.
