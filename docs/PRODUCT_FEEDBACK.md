# Product Feedback / Friction Summary

## Android and Gradle

The Android toolchain was straightforward once a complete JDK was installed. The main friction was that a Java runtime alone looked healthy but Gradle could not find the `JAVA_COMPILER` capability. Setup documentation should explicitly ask developers to verify both `java --version` and `javac --version`.

## Amazon Fire TV platform guidance

The downloaded Amazon device skills were useful for emphasizing D-pad focus, TV-safe spacing, and the difference between Fire OS Android development and Vega React Native development. The main limitation is that several Builder Tools workflows require the live MCP server and a Vega SDK, while this project intentionally targets native Fire OS Android/Kotlin. A clear Fire OS versus Vega support matrix would reduce setup ambiguity.

## Backend / AI boundary

Keeping the local catalog as the source of truth makes the AI contract safer. The optional Bedrock adapter is designed to rank and explain known candidates rather than invent title metadata. A useful improvement would be a documented reference deployment for an HTTPS API Gateway/Lambda path with least-privilege Bedrock permissions and a standard structured-output schema.

## Fire TV testing

The project can be packaged and its manifest can declare lean-back support, but physical remote, overscan, performance, and network reachability checks require hardware or a compatible simulator. A maintained Fire TV virtual-device option would reduce the gap between APK build verification and actual TV UX verification.
