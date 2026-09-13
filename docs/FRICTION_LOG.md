# FireMind Friction Log

## 2026-09-13 — Java compiler detection

| Field | Details |
|---|---|
| Tool | Gradle 9.7.1 and Android Gradle Plugin |
| Problem | The first Android build failed because the environment contained a Java runtime but Gradle could not find the Java compiler capability. |
| Expected | `assembleDebug` should locate a complete JDK and compile the Kotlin/Java sources. |
| Actual | Gradle reported that `/usr/lib/jvm/java-21-openjdk-amd64` did not provide `JAVA_COMPILER`. |
| Impact | The first two build attempts stopped before Kotlin compilation. |
| Workaround | Installed `openjdk-21-jdk-headless`, stopped the stale Gradle daemon, set `JAVA_HOME`, and rebuilt. |
| Result | The subsequent build completed successfully and produced `app/build/outputs/apk/debug/app-debug.apk`. |
| Documentation improvement | Fire TV setup instructions should explicitly verify `javac`, not only `java`, before running Gradle. |
