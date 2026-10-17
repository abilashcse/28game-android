# Thuruppugulan (KMP)

Kotlin Multiplatform + Compose Multiplatform port of **Thuruppugulan** (28 / Lelam) for **Android** and **iOS**.

## Modules

| Module | Role |
|--------|------|
| `shared` | Domain model, game engine, Ktor WebSocket client |
| `composeApp` | Shared Compose UI (Material 3) + iOS framework |
| `androidApp` | Android application entry |
| `iosApp` | Xcode / SwiftUI host for Compose |
| `legacy/android-java` | Original Java Android prototype (archived) |

## Features in this refactor

- Shared pure-Kotlin rules engine (`ThuruppuGame`) with bidding, concealed trump, trick play, and chaka-style match scoring
- Offline mode with simple bots so you can play a full round without a server
- Optional online table via the same achex-style WebSocket hub pattern as the legacy app
- Compose Multiplatform lobby + felt table UI for Android & iOS

## Requirements

- JDK 17+
- Android Studio / AGP-compatible SDK (compileSdk 35)
- Xcode 15+ for iOS
- Gradle wrapper included (`./gradlew`)

## Run Android

```bash
./gradlew :androidApp:assembleDebug
# or open the repo in Android Studio and run `androidApp`
```

## Run iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Set your development team in `iosApp/Configuration/Config.xcconfig` if needed
3. Build & run on a simulator or device  
   The Xcode build phase calls `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`

## Tests

```bash
./gradlew :shared:cleanAllTests :shared:allTests
```

## Game rules

Full implemented rules: see [`RULES.md`](RULES.md).

Four players in fixed partnerships (you + partner vs two opponents). 32-card deck ranked **J-9-A-10-K-Q-8-7**. Points: J=3, 9=2, A=1, 10=1 (28 total). Auction, concealed trump, expose, doubles, overtrump, cancel conditions, and chaka scoring to ±6 are all in the shared engine.

Offline mode: **you + 3 bots** play a complete match.

## Package

`com.agames.thuruppugulan` · version `2.0.0-kmp`
