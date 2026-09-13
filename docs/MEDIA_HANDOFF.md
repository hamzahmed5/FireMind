# Media Handoff

Runtime media has now been captured from the FireMind APK installed on an Android TV 10 x86 emulator using software CPU emulation. The media proves the app renders at 1920×1080, shows the Home, Recommendations, Details, and Watchlist states, and persists Sing Street after the Add to Watchlist action.

The capture is compatible Android TV validation, not physical Fire TV hardware validation. A physical Fire TV is still required for final Fire OS overscan, hardware performance, and real-remote verification.

| File | Content |
|---|---|
| `home-after-wait.png` | Rendered Home screen after emulator startup settles. |
| `assistant.png` | Recommendations screen after the Funny quick mood. |
| `details.png` | Sing Street details with Add to Watchlist state. |
| `watchlist-final-verified.png` | Live Watchlist page with Sing Street, verified by UI hierarchy. |
| `firemind-demo.mp4` | Real screen recording of Home → Recommendations → Details. |
| `firemind-clean-demo-final.mp4` | Real clean-state screen recording with Details and saved-state transition. |

The final combined evidence video is assembled from the real runtime recording plus the real captured Watchlist frame so that the final media clearly includes the saved-title state. It is labeled as evidence media rather than a claim of physical Fire TV hardware capture.
