# example-app

A Kotlin Multiplatform example app that demonstrates the OpenTelemetry Kotlin API. It emits
spans and logs that are then sent to OpenTelemetry exporters that by default print the telemetry.

## Exporting to a Collector

By default, the example app prints telemetry to stdout. You can optionally export telemetry to an
OpenTelemetry collector by setting the `url` property in `AppConfig.kt`:

```kotlin
val url: String? = "http://localhost:4318"
```

To run a collector locally,
see [opentelemetry-collector](https://github.com/open-telemetry/opentelemetry-collector).

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
