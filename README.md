![Cover](https://github.com/Ninidze1/Chess-Compose-Multiplatform/assets/73029688/7c60306d-64a4-4896-b116-8c8e98a30dab)

# Chess

This project is a digital implementation of the classic board game Chess. The project is built on top of the compose multiplatform.

## Building

The toolchain is pinned in [`gradle/libs.versions.toml`](gradle/libs.versions.toml): Kotlin 2.4, Compose Multiplatform 1.12, AGP 8.13, Gradle 8.14 (through the wrapper), compileSdk/targetSdk 36 and minSdk 24.

Requirements:

- JDK 17 or newer to run Gradle.
- Android Studio (or the Android SDK with API 36) for the Android app.
- macOS with a current Xcode and [CocoaPods](https://cocoapods.org) for the iOS app. iOS 14.1 or newer, on a device or an Apple silicon simulator (Intel simulators are not supported by Compose Multiplatform).

Android:

```sh
./gradlew :androidApp:installDebug
```

iOS: the `shared` framework is integrated as a local pod. The `Pods/` directory is not committed, so generate it once, and again after changing the shared module's dependencies or resources:

```sh
./gradlew :shared:podInstall    # or: cd iosApp && pod install
open iosApp/iosApp.xcworkspace  # always open the workspace, not the project
```

## Testing and lint

The game rules, persistence and the ViewModel turn flow are covered by `shared/src/commonTest`, which runs on the JVM and on the iOS simulator:

```sh
./gradlew :shared:testDebugUnitTest      # JVM
./gradlew :shared:iosSimulatorArm64Test  # iOS simulator, macOS only
```

Code style is checked with [ktlint](https://pinterest.github.io/ktlint/), configured in [`.editorconfig`](.editorconfig). The ported chess engine (`domain/engine`) is excluded.

```sh
./gradlew ktlintCheck    # report
./gradlew ktlintFormat   # fix what can be fixed automatically
```

GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) runs the tests and the Android debug build on every push to `main` and every pull request, plus ktlint as an advisory job.

## Contributing

Contributions are welcome! If you have a feature you'd like to add, or you've found a bug and know how to fix it, please feel free to fork the repository and submit a pull request. For significant changes, please open an issue first to discuss your proposed changes. 

## Future Plans

The project has several features planned for future development:

- ~~**AI Opponent**: An AI opponent will be added to the game, allowing players to test their skills against a computer opponent.~~
- ~~**Local Storage**: Game progress will be saved in local storage, allowing players to pick up where they left off.~~
- **Multiplayer**: Multiplayer functionality will be added, allowing two players to play against each other on different devices.
- ~~**Animations**: The game will incorporate animations to enhance the user experience and make the game more visually appealing~~.

---
