# FireMind — Devpost Submission Draft

## Project name

FireMind

## Elevator pitch

An AI-powered viewing companion that helps Fire TV users discover and understand what to watch without scrolling through endless choices.

## Inspiration

TV discovery is often slow because remote-based typing is painful and large catalogs create decision fatigue. Viewers may know the mood, time limit, or audience they have in mind without knowing the title they want.

## What we built

FireMind is a TV-first viewing companion. A viewer can choose a quick mood or enter a natural-language request such as “I want a mind-bending sci-fi movie under two hours.” FireMind returns a short, grounded set of recommendations, explains why each title matches, opens a detail view with a spoiler-safe summary, and saves titles to a local watchlist.

## How it works

The native Android/Kotlin Fire OS app uses a curated catalog and deterministic fallback ranking. The backend exposes a stable JSON API for health, recommendations, summaries, and similar content. An optional Bedrock adapter can rank and explain only known catalog candidates, while malformed, unavailable, or unauthorized AI responses fall back to local results.

## Built for Fire TV

The interface uses large typography, high contrast, generous TV-safe spacing, landscape orientation, remote-first quick prompts, explicit focus styling, and Back navigation. No mouse or touch interaction is required by the core flow.

## Built with

- Amazon Fire TV / Fire OS target
- Android
- Kotlin
- Gradle
- Python standard-library backend
- Optional Amazon Bedrock adapter through AWS SDK for Python
- JSON REST API
- Local SharedPreferences watchlist persistence

## What we learned

TV interaction design requires treating focus as a primary state rather than a side effect. A reliable catalog-grounded fallback is important because a recommendation experience should not freeze when a network or model call fails. A complete build-and-test loop also requires verifying the Java compiler, Android SDK, packaging metadata, and device connectivity instead of assuming that a successful source edit means the app is ready.

## Challenges

The sandbox initially contained a Java runtime but not a complete compiler toolchain, so Gradle could not create the Java compiler task. Installing the JDK, refreshing the Gradle daemon, and rerunning the build resolved the issue. Physical Fire TV validation still requires a real device or compatible test environment.

## What's next

Connect the backend to a deployed HTTPS endpoint, validate the APK on Fire TV hardware with ADB, capture screenshots and a sub-three-minute demo video, and publish the verified repository and submission media.

## Current limitation disclosure

The current sandbox verification proves the Android build, APK metadata, backend endpoints, fallback behavior, and security checks. It does not claim physical Fire TV installation, real remote testing, public GitHub publication, or live Bedrock execution until those external steps are completed.
