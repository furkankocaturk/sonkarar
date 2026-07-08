# AGENTS.md

## Cursor Cloud specific instructions

SonKarar is a **native Android app** (Kotlin + Jetpack Compose, Gradle). There is no server to run; "running" means building the APK and exercising logic via unit tests. Standard build/test commands live in `README.md` ("Kurulum Notları"); the notes below only cover non-obvious cloud caveats.

### Toolchain (already provisioned in the VM snapshot)
- **JDK 17** is required by AGP 8.7.3 and is the system default (`java`/`javac` → `/usr/lib/jvm/java-17-openjdk-amd64`). Do not build with JDK 21.
- **Android SDK** lives at `~/android-sdk` (`platform-tools`, `platforms;android-35`, `build-tools;35.0.0`). Interactive shells export `JAVA_HOME`/`ANDROID_HOME` via `~/.bashrc`.
- `local.properties` (git-ignored) points Gradle at the SDK via `sdk.dir`. Recreate it with `sdk.dir=/home/ubuntu/android-sdk` if missing.

### Required config files (git-ignored, kept only in the VM snapshot)
- `app/google-services.json` is a **placeholder** dev file so the `com.google.gms.google-services` Gradle plugin (and thus every build) succeeds. It is NOT a real Firebase project. Firebase Auth / Firestore features cannot be tested end-to-end without a real `google-services.json` + web client ID.
- `TMDB_API_ANAHTARI` in `local.properties` is optional; without it the app falls back to the local pool (see `README.md`).

### What works headlessly
- `./gradlew testDebugUnitTest` — JVM unit tests covering the core weighted-decision engine (`AgirlikliSecimYap`, `ZamanCezasi`). This is the best "run the core logic" check.
- `./gradlew lintDebug` — Android lint.
- `./gradlew assembleDebug` — produces `app/build/outputs/apk/debug/app-debug.apk`.

### What does NOT work here
- There is **no `/dev/kvm`** in the cloud VM, so an Android emulator cannot boot. Instrumented/UI tests (`connectedDebugAndroidTest`) and interactively running the app require a real device/emulator and cannot be done in this environment.

### Note on branches
The actual project currently lives on the `cursor/sonkarar-mimari-sartname-a9a8` feature branch; `main` only has a placeholder `README.md`. Build/test commands only apply where the Gradle project (`gradlew`, `app/`) is present.
