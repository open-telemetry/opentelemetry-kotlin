# Getting started

This guide helps you integrate OpenTelemetry Kotlin into your project so that you can see telemetry
in a dashboard. Platform-specific instructions can be found at the end of the document.

- [1. Pick a mode](#1-pick-a-mode)
- [2. Install](#2-install)
- [3. Opt in to experimental APIs](#3-opt-in-to-experimental-apis)
- [4. Initialize the SDK](#4-initialize-the-sdk)
- [5. Record a span and a log](#5-record-a-span-and-a-log)
- [6. Send telemetry over OTLP](#6-send-telemetry-over-otlp)
- [7. Flush and shut down](#7-flush-and-shut-down)
- [Platform notes](#platform-notes)
    - [Android](#android)
    - [iOS](#ios)

## 1. Pick a mode

opentelemetry-kotlin has two SDK implementations behind the same API. Implementation is the preferred
mode but you can use compat if you already have an existing OpenTelemetry Java SDK integration.

Compat mode is created with `createCompatOpenTelemetry` instead of `createOpenTelemetry` and is
configured with the same DSL. See the [compat mode guide](compat.md) for more differences.

## 2. Install

Import the BOM then add the SDK and OTLP exporter:

```kotlin getting-started-install
dependencies {
    implementation(platform("io.opentelemetry.kotlin:bom:0.10.0"))
    implementation("io.opentelemetry.kotlin:implementation")
    implementation("io.opentelemetry.kotlin:exporters-otlp")
}
```

For compat mode, depend on `compat` rather than `implementation`. See the
[compat mode guide](compat.md#installing).

## 3. Opt in to experimental APIs

The SDK's APIs are marked `@ExperimentalApi` while the project is pre-1.0, which produces
compiler warnings to alert you the project's API is not stable and can change without warning. You
can suppress this warning for a whole module like this:

```kotlin getting-started-opt-in
kotlin {
    compilerOptions {
        optIn.add("io.opentelemetry.kotlin.ExperimentalApi")
    }
}
```

Alternatively you should opt-in on a case-by-case basis:

```kotlin getting-started-opt-in-call-site
@OptIn(ExperimentalApi::class)
fun recordStartup(otel: OpenTelemetry) {
    otel.tracerProvider.getTracer("com.example.startup").startSpan("app-start").end()
}
```

## 4. Initialize the SDK

Create one SDK instance as soon as your app starts, and retain a reference for later use:

```kotlin getting-started-init
fun initTelemetry(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service" // shown as service.name in your backend
    tracerProvider {
        export { simpleSpanProcessor(stdoutSpanExporter()) }
    }
    loggerProvider {
        export { simpleLogRecordProcessor(stdoutLogRecordExporter()) }
    }
}
```

See [platform-notes](#platform-notes) for instructions on where to initialize for a specific platform.

## 5. Record a span and a log

Get a tracer and a logger from the SDK and record telemetry:

```kotlin getting-started-first-telemetry
fun checkout(otel: OpenTelemetry) {
    val tracer = otel.tracerProvider.getTracer("com.example.checkout")
    val logger = otel.loggerProvider.getLogger("com.example.checkout")

    val span = tracer.startSpan("checkout")
    try {
        span.setStringAttribute("cart.id", "cart-42")
        logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
    } finally {
        span.end() // a span is only exported once it ends
    }
}
```

Run your app and the span and log should print to stdout. On Android this will be Logcat; on iOS it 
will be the XCode console.

## 6. Send telemetry over OTLP

You can now try sending data to an OTLP-enabled backend:

```kotlin getting-started-otlp
const val COLLECTOR_URL = "http://localhost:4318" // use http://10.0.2.2:4318 on the Android emulator

fun initTelemetryWithOtlp(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = COLLECTOR_URL }) }
    }
    loggerProvider {
        export { batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = COLLECTOR_URL }) }
    }
}
```

`endpoint` is the collector's base URL. The exporter adds `/v1/traces` or `/v1/logs`.

If you don't have an OTLP backend to test against, you can use the project's
[local-collector](../examples/local-collector) example app. It runs on `localhost:4318` and displays
basic telemetry.

## 7. Flush and shut down

Batch processors hold telemetry in memory before exporting so you need to flush when the process
is about to terminate. Two calls control this:

```kotlin getting-started-flush
// Exports everything buffered so far. The SDK keeps running.
suspend fun flushTelemetry(otel: OpenTelemetrySdk) {
    otel.forceFlush()
}

// Flushes, then stops the SDK. Telemetry recorded afterwards is dropped.
suspend fun stopTelemetry(otel: OpenTelemetrySdk) {
    otel.shutdown()
}
```

Both are `suspend` functions and must be called by your app at a suitable point. As a general rule:

- Call `forceFlush()` when your app might be killed without warning.
- Call `shutdown()` once, when a process ends normally, e.g. at the end of a JVM or Node `main`.

## Platform notes

### Android

Create the SDK in `Application.onCreate`, so it exists before any activity starts, and register
the application class in `AndroidManifest.xml`:

```kotlin getting-started-android
class MyApplication : Application() {
    lateinit var otel: OpenTelemetrySdk

    override fun onCreate() {
        super.onCreate()
        otel = initTelemetryWithOtlp()
    }
}
```

On the emulator, `localhost` is the emulator itself. Use `http://10.0.2.2:4318` to reach a
collector running on your machine.

Android blocks plain HTTP by default. For local development, allow it for the collector's host in
`res/xml/network_security_config.xml`, and reference that file from the `<application>` element
with `android:networkSecurityConfig="@xml/network_security_config"`:

```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

### iOS

The SDK is used from Kotlin. Initialize it in the KMP module that your iOS app links as a
framework, and expose a small API of your own for Swift to call:

```kotlin getting-started-ios-kotlin
// In the shared KMP module that your iOS framework is built from.
object Telemetry {
    private var otel: OpenTelemetrySdk? = null

    fun start() {
        if (otel == null) {
            otel = initTelemetryWithOtlp()
        }
    }
}
```

Build that module as a framework:

```kotlin getting-started-ios-framework
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared" // the module name Swift imports
        }
    }
}
```

Then start the SDK when the app launches:

```swift getting-started-ios-swift
import SwiftUI
import Shared

@main
struct MyApp: App {
    init() {
        Telemetry.shared.start()
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

You only need to export opentelemetry-kotlin from the framework (with `export(...)`) if Swift
code uses its types directly. The simulator reaches a collector on your machine at
`http://localhost:4318`.
