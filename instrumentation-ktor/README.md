# instrumentation-ktor

This module provides OpenTelemetry instrumentation for the Ktor client.

## Installation

Add the dependency to your project:

```kotlin
implementation("io.opentelemetry.kotlin:instrumentation-ktor:0.2.0") // verify version
```

## Usage

Install the `OpenTelemetryKtor` plugin in your Ktor `HttpClient`:

```kotlin
val openTelemetry = // Obtain your OpenTelemetry instance

val client = HttpClient {
    install(OpenTelemetryKtor) {
        this.openTelemetry = openTelemetry
    }
}
```

## Features

- Automatically creates a CLIENT span for each outgoing request.
- Populates standard HTTP semantic convention attributes:
    - `http.request.method`
    - `url.full`
    - `server.address`
    - `server.port`
    - `url.scheme`
    - `http.response.status_code`
    - `http.request.body.size`
    - `http.response.body.size`
- Handles exceptions and records them as span events.
- Sets span status based on HTTP status code (Error if >= 400) or exceptions.

## Current Limitations

- **Propagation**: Trace context propagation (injecting headers like `traceparent`) is not yet implemented in this initial version, as it depends on the core propagation API which is currently being finalized.
- **Server Instrumentation**: This module currently only provides client-side instrumentation.
