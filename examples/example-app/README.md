# example-app

A Kotlin Multiplatform example app that demonstrates the OpenTelemetry Kotlin API. It emits
spans and logs and exports them via OTLP/HTTP with `service.name` set to `otel-kotlin-example`.

## Viewing the telemetry

The app sends telemetry to [local-collector](../local-collector/README.md). Start it before running
any of the targets below:

```sh
./gradlew :examples:local-collector:run
```

The UI apps (desktop, Android and iOS) batch telemetry and flush it when the app goes to the
background. The console and Node.js apps flush before they exit.

## Configuring the example app

### Changing the collector URL

The app exports to `http://localhost:4318`, or `http://10.0.2.2:4318` on Android (the emulator's
address for the host). To send telemetry somewhere else, such as an
[OpenTelemetry Collector](https://github.com/open-telemetry/opentelemetry-collector), change the
`url` property in `AppConfig.kt`:

```kotlin
val url: String = "http://my-collector:4318"
```

### Altering the SDK mode

opentelemetry-kotlin has 1 API with 3 implementations. By default the KMP implementation will be
used but you can change to the compat (uses opentelemetry-java under the hood) or no-op
implementation by setting the `sdkMode` property in `AppConfig.kt`:

```kotlin
val sdkMode = SdkMode.COMPAT
```

### Using custom processors

You can supply your own processors by updating [`createSpanProcessor()`](src/commonMain/kotlin/io/opentelemetry/example/app/ProcessorFactory.kt#L22)
and [`createLogRecordProcessor()`](src/commonMain/kotlin/io/opentelemetry/example/app/ProcessorFactory.kt#L32).
[`TelemetryEntrypoint.kt`](src/commonMain/kotlin/io/opentelemetry/example/app/TelemetryEntrypoint.kt#L14)
calls these functions when configuring the SDK.

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

Install the Android app on an emulator, then launch it manually. See
[example-app-android](../example-app-android/README.md) for physical devices.

```sh
./gradlew :examples:example-app:runAndroidExampleApp
```

### JS (Node.js)

```sh
./gradlew :examples:example-app:runNodeExampleApp
```

### iOS

Build and run the iOS app in the simulator. This requires macOS with Xcode 26 or newer and an
iPhone simulator running iOS 15 or newer. The script picks an iPhone on the newest suitable
runtime and opens Simulator (DeviceHub on Xcode 27 and newer):

```sh
./gradlew :examples:example-app:runIosExampleApp
```
