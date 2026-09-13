# FireMind Testing

## Verified in the development sandbox

| Area | Result | Evidence |
|---|---|---|
| Android build | Passed | `gradle clean assembleDebug` |
| APK metadata | Passed | `aapt2 dump badging` reports `com.firemind`, target SDK 35 |
| Backend syntax | Passed | `python3 -m py_compile backend/server.py` |
| Backend health | Passed | `GET /api/health` returned HTTP 200 |
| Recommendation contract | Passed | `POST /api/recommend` returned grounded catalog results |
| Summary contract | Passed | `POST /api/summarize` returned spoiler-safe summary |
| Validation | Passed | Missing query returned HTTP 400 with `query_required` |
| Secret filename audit | Passed | No `.env`, credential, keystore, or certificate files in project |

## Required hardware or emulator verification

The following checks require a real Fire TV device or compatible Android TV environment and are not claimed as complete in the sandbox:

- Install the APK through ADB.
- Cold launch and warm launch on Fire TV hardware.
- D-pad UP, DOWN, LEFT, RIGHT, SELECT, and BACK behavior.
- Focus visibility at a ten-foot viewing distance.
- Overscan and safe-zone inspection.
- Actual Fire OS network reachability to the backend.
- App restart persistence on device.
- Performance, memory, and animation feel under TV hardware constraints.
- Screenshot and demo-video capture on the intended platform.
