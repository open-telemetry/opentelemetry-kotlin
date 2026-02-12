# example-app

A Kotlin Multiplatform example app that demonstrates the OpenTelemetry Kotlin API. It emits
spans and logs that are then sent to OpenTelemetry exporters that by default print the telemetry.

## Example app targets

### Desktop (JVM)

```sh
./gradlew :examples:example-app:runJvmExampleApp
```

### Console (JVM)

```sh
./gradlew :examples:example-app:runConsoleExampleApp
```

### Android

Install and run the Android app on a connected device or emulator:

```sh
./gradlew :examples:example-app:runAndroidExampleApp
```

Then launch it manually from your device/emulator.

### JS (Node.js)

```sh
./gradlew :examples:example-app:runNodeExampleApp
```

### iOS

Build and run the iOS app in the simulator (requires macOS with Xcode):

```sh
./gradlew :examples:example-app:runIosExampleApp
```
