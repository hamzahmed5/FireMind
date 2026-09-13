# FireMind Release Checklist

## Complete in the sandbox

- [x] Native Android/Kotlin Fire OS app shell.
- [x] Home, Assistant, Recommendations, Details, Watchlist, and About screens.
- [x] Grounded local catalog and deterministic fallback ranking.
- [x] Optional Bedrock adapter with response validation and fallback.
- [x] Health, recommend, summarize, and similar API endpoints.
- [x] Gradle wrapper and clean debug build.
- [x] Backend endpoint tests.
- [x] APK metadata inspection.
- [x] Secret filename and staged-content scans.
- [x] README, architecture, API specification, testing, friction, demo, Devpost, and product-feedback documents.
- [x] MIT license.
- [x] APK installed on Android TV 10 x86 emulator through ADB.
- [x] Home, Recommendations, Details, and Watchlist rendered at 1920×1080.
- [x] D-pad-style navigation exercised with emulator key events.
- [x] Add-to-Watchlist state changed to Remove and persisted title appeared in Watchlist.
- [x] Runtime screenshots captured.
- [x] H.264 demo recording captured.
- [x] Evidence reel verified to show the saved Watchlist state.

## Requires a physical Fire TV device

- [ ] Install APK on physical Fire TV.
- [ ] Test physical remote feel and Back behavior.
- [ ] Verify overscan-safe layout on the target television.
- [ ] Verify performance and memory on Fire OS hardware.
- [ ] Verify backend reachability from the physical device.

## Requires AWS access

- [ ] Configure AWS credentials through a secure profile or runtime role.
- [ ] Select an approved Bedrock model.
- [ ] Deploy the backend behind HTTPS.
- [ ] Connect the Fire TV app to the deployed backend.
- [ ] Run one live Bedrock recommendation and retain the response evidence.

## Requires external publishing access

- [ ] Create or select the public GitHub repository.
- [ ] Push the verified source and release APK if desired.
- [ ] Upload the evidence video to YouTube or Vimeo.
- [ ] Complete the Devpost fields and media gallery.
- [ ] Add the actual repository and demo URLs to the submission.
