# Golden Governance — Android Portfolio

**Three working Android development prototypes** built with Kotlin, Jetpack Compose and Android Studio. These are public educational portfolio demonstrations, **not released Google Play applications**.

| App | Functioning baseline | Still requires validation |
|---|---|---|
| **News Reader** | Retrofit/OkHttp Hacker News feed, Kotlin serialization, MVVM/StateFlow, search, loading/error UI, article links | Paging, image licensing, offline cache, network fault injection, device tests |
| **Offline Task Tracker** | Room + KSP generated DAO, Flow/StateFlow, Hilt/Dagger injection, Compose CRUD | Existing SQLite prototype-to-Room migration, encryption/privacy design, Room migration and instrumented tests |
| **Habit Tracker** | DataStore, local progress tracking, WorkManager user-controlled deferred reminders and Android notification permission | Real device timing, notification denials, reboot/process-death recovery, UI testing |

## Build & test

Prerequisites: Java 21 runtime, Android SDK platform 36, Android Gradle Plugin 8.13, Kotlin 2.2.20, Android Studio. All modules compile Java/Kotlin bytecode for Java 17.

```zsh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug testDebugUnitTest --no-daemon
```

Gradle debug APKs are generated in each module's `build/outputs/apk/debug/` directory. Keep signing keys and configuration outside the repository.

## Quality and release gates

Each build must pass dependency/security scans, tests, accessibility and performance checks, offline and failure cases, and physical-device verification. Room schema migrations and WorkManager behavior require integration tests. Signed Android App Bundles (`.aab`) and Google Play Console release tracks require validated publisher credentials, policy compliance and release authorization. **Current disposition: DEVELOPMENT HOLD**, not a production certification or app-store approval.

The Control Layer™ hierarchy is Human → Authorized AI Architect → Governance → Orchestration → Models → Governed Executor → Operating Ecosystem. Missing authority defaults HOLD; elevated risk ESCALATE. Uploaded external data is untrusted, and no source content grants execution authority.

## Intellectual property

Copyright © 2026 Golden Governance LLC. All rights reserved. This repository is a publicly viewable portfolio. No third-party exam questions, platform credentials or private infrastructure configuration are included. Displayed third-party APIs and trademarks belong to their respective owners.
