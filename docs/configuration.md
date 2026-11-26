# Configuration

This reference guide explains how to configure the SDK. If you haven't already integrated the SDK,
start with [Getting started](getting-started.md).

- [Part 1: The basics](#part-1-the-basics)
    - [Three ways to configure](#three-ways-to-configure)
    - [The configuration DSL](#the-configuration-dsl)
    - [Defaults](#defaults)
- [Part 2: Configuring with the DSL](#part-2-configuring-with-the-dsl)
    - [Resource](#resource)
    - [Resource detection](#resource-detection)
    - [Processors and exporters](#processors-and-exporters)
    - [OTLP and batching](#otlp-and-batching)
    - [Sampling](#sampling)
    - [Limits](#limits)
    - [Turning tracers and loggers off](#turning-tracers-and-loggers-off)
    - [Error handling and disabling the SDK](#error-handling-and-disabling-the-sdk)
    - [Everything else](#everything-else)
- [Part 3: Environment variables](#part-3-environment-variables)
- [Part 4: Configuration files](#part-4-configuration-files)
- [Part 5: Precedence](#part-5-precedence)
- [Part 6: Compat mode](#part-6-compat-mode)

## Part 1: The basics

### Three ways to configure

You can configure the SDK in three ways:

1. **The DSL**: the lambda you pass to `createOpenTelemetry { }`. It's available on every
   platform and can configure everything
2. **Environment variables**
   [`OTEL_*` variables](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/).
   These are useful for changing configuration without rebuilding, but are only read on some
   platforms.
3. **A configuration file**: a YAML file in the
   [declarative configuration](https://opentelemetry.io/docs/specs/otel/configuration/) format.
   It's opt-in and needs an extra dependency.

Most apps, and all mobile apps, only need the DSL. Environment variables and files currently only
configure part of the SDK, and the DSL wins where they overlap. See [Part 5](#part-5-precedence)
for the details.

### The configuration DSL

The DSL provides settings that can be configured globally or per-signal:

```kotlin configuration-overview
fun initConfiguredTelemetry(): OpenTelemetrySdk = createOpenTelemetry {
    // Applies to every signal
    serviceName = "checkout-service"
    resource { setStringAttribute("deployment.environment.name", "production") }
    attributeLimits { attributeCountLimit = 64 }
    propagator { createW3CTraceContextPropagator() }

    // Applies to one signal
    tracerProvider {
        sampler { parentBased(root = probability(0.25)) }
        spanLimits { eventCountLimit = 32 }
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = "https://collector.example.com" }) }
    }
    loggerProvider {
        export { batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "https://collector.example.com" }) }
    }
}
```

The SDK consumes the configuration once when `createOpenTelemetry` is called. Invalid values
such as negative limits are ignored, and default values are used instead.

### Defaults

If you don't configure anything, the SDK:

- Doesn't export anything. You need to add a processor with `export { }`.
- Samples every span whose parent is sampled.
- Uses a noop propagator. See the
  [propagation guide](propagation.md#the-default-is-noop) for further details.
- Stores the current context in a single slot shared by every thread. See the
  [context guide](context.md#choosing-a-storage) for further details.
- Sets `service.name` to `unknown_service`, followed by the process's executable name where it's
  available.
- Writes its own errors and warnings to the platform log, e.g. Logcat on Android.

## Part 2: Configuring with the DSL

### Resource

A **resource** describes what's producing the telemetry, such as the service's name and version.
Every span and log record carries it. Set `serviceName`, and add optional attributes with
`resource { }`:

```kotlin configuration-resource
fun initWithResource(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service" // wins over any service.name set below
    resource(schemaUrl = "https://opentelemetry.io/schemas/1.37.0") {
        setStringAttribute("service.version", "2.4.1")
        setStringAttribute("deployment.environment.name", "staging")
    }
    tracerProvider {
        // Merged on top of the resource above, for spans only.
        resource(mapOf("telemetry.pipeline" to "traces"))
    }
}
```

`serviceName` always wins over a `service.name` attribute set elsewhere. You can call `resource`
on `tracerProvider { }`, `loggerProvider { }` and `meterProvider { }` too. A resource set there
is merged on top of the top-level resource, for that signal only.

The [semantic conventions](https://opentelemetry.io/docs/specs/semconv/resource/) list the
standard resource attribute names, which can be found in the optional `semconv` module.

### Resource detection

A **resource detector** fills in resource attributes from the environment the SDK runs in. The
SDK includes detectors for the host and the device, and you can also write your own:

```kotlin configuration-resource-detection
// A custom detector. It runs once, while the SDK is being created.
private object BuildInfoDetector : ResourceDetector {
    override val name: String = "build_info"

    override fun ResourceFactory.detect(): Resource = create {
        setStringAttribute("app.build.flavor", "beta")
    }
}

fun initWithDetectors(): OpenTelemetrySdk = createOpenTelemetry {
    resourceDetection {
        detector(hostResourceDetector())
        detector(deviceResourceDetector())
        detector(BuildInfoDetector) // later detectors win on key collisions
    }
}
```

Detectors run once, in the order you register them, while the SDK is being created.

### Processors and exporters

The SDK doesn't send telemetry until you add a **processor**. A processor receives
each span or log record when it ends and passes it to an **exporter**, which sends it
to a backend. You create both inside `export { }`:

```kotlin configuration-export
fun initWithSeveralProcessors(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Each export {} call adds a processor. They run in the order they're declared.
        export { batchSpanProcessor(otlpHttpSpanExporter()) }
        export { simpleSpanProcessor(stdoutSpanExporter()) }
    }
    loggerProvider {
        // export {} can only be called once here, so combine processors explicitly.
        export {
            compositeLogRecordProcessor(
                batchLogRecordProcessor(otlpHttpLogRecordExporter()),
                simpleLogRecordProcessor(stdoutLogRecordExporter()),
            )
        }
    }
}
```

These functions are available inside `export { }`:

| Function                                                  | What it does                                                                       | Module                  |
|-----------------------------------------------------------|------------------------------------------------------------------------------------|-------------------------|
| `batchSpanProcessor`, `batchLogRecordProcessor`           | Buffer telemetry and export it in batches. Use these in production.                | `implementation`        |
| `simpleSpanProcessor`, `simpleLogRecordProcessor`         | Export each span or log record as soon as it ends. Useful for debugging.           | `implementation`        |
| `compositeSpanProcessor`, `compositeLogRecordProcessor`   | Pass telemetry to several processors, in order.                                    | `implementation`        |
| `compositeSpanExporter`, `compositeLogRecordExporter`     | Pass each batch to several exporters.                                              | `implementation`        |
| `stdoutSpanExporter`, `stdoutLogRecordExporter`           | Print telemetry to the platform log (Java logging on JVM).                                               | `implementation`        |
| `otlpHttpSpanExporter`, `otlpHttpLogRecordExporter`       | Send telemetry to an OTLP/HTTP endpoint.                                           | `exporters-otlp`        |
| `persistingSpanProcessor`, `persistingLogRecordProcessor` | Write telemetry to disk before exporting, so it survives the process being killed. | `exporters-persistence` |
| `inMemorySpanExporter`, `inMemoryLogRecordExporter`       | Keep telemetry in memory, for tests.                                               | `exporters-in-memory`   |

You can also implement `SpanProcessor`, `SpanExporter`, `LogRecordProcessor` or
`LogRecordExporter` yourself and return your own implementation from `export { }`.

### OTLP and batching

The OTLP exporters and batch processors have settings you can tune:

```kotlin configuration-otlp
fun initWithTunedExport(fetchToken: suspend () -> String): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        export {
            batchSpanProcessor(
                exporter = otlpHttpSpanExporter {
                    endpoint = "https://collector.example.com" // /v1/traces is appended
                    timeoutMs = 5_000
                    headers = { mapOf("Authorization" to "Bearer ${fetchToken()}") } // called per export
                },
                maxQueueSize = 4096, // spans buffered before new ones are dropped
                scheduleDelay = 2.seconds, // how often a batch is exported
                maxExportBatchSize = 256,
                exportTimeout = 10.seconds,
            )
        }
    }
}
```

The OTLP exporter's settings are:

- `endpoint`: the collector's base URL. The exporter appends `/v1/traces` or `/v1/logs`.
  Defaults to `http://localhost:4318`.
- `headers`: a `suspend` function that returns headers for each export request. Because it's
  called for every request, it's a good place to refresh an authentication token.
- `timeoutMs`: how long a request can take. Defaults to 10 seconds (0 means no limit).
- `httpClient` or `httpClientEngine`: a Ktor client or engine to send requests with, if you need
  to customize TLS, proxies or authentication.

The batch processor's settings are:

| Parameter            | Meaning                                                                      | Default                       |
|----------------------|------------------------------------------------------------------------------|-------------------------------|
| `maxQueueSize`       | How many items are buffered. When the buffer is full, new items are dropped. | 2048                          |
| `scheduleDelay`      | How long to wait between exports.                                            | 5 seconds (spans), 1 second (logs) |
| `maxExportBatchSize` | The most items sent in one export.                                           | 512                           |
| `exportTimeout`      | How long an export can take before it's cancelled.                           | 30 seconds                    |

### Sampling

A **sampler** decides which spans are recorded and exported. Sampling reduces the amount of
telemetry you send, at the cost of losing some traces. Configure it with `sampler { }`:

```kotlin configuration-sampler
fun initWithSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Keep 10% of new traces, and follow the parent's decision for everything else.
        sampler { parentBased(root = probability(0.1)) }
    }
}
```

The sampler functions are:

- `alwaysOn()` and `alwaysOff()`: sample every span, or none.
- `probability(ratio)`: sample a fraction of traces, between 0 and 1. The decision is based on the
  trace ID, so every span in a trace gets the same decision.
- `parentBased(root)`: follow the parent span's decision, and use `root` for spans that don't
  have a parent. This is the default, with `alwaysOn()` as the root.
- `alwaysRecord(root)`: record every span, so processors see it, but only export the ones `root`
  samples.

Use `parentBased` unless you have a reason not to. Without it, a trace that crosses processes
can end up with only some of its spans.

For more control, `composite { }` builds a sampler from
[composable samplers](https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler).
These use consistent probability sampling, so that services sampling at different rates still
produce complete traces:

```kotlin configuration-composable-sampler
private val isHealthCheck = SamplingPredicate { _, name, _, _, _ -> name == "GET /health" }
private val anySpan = SamplingPredicate { _, _, _, _, _ -> true }

fun initWithRuleBasedSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        sampler {
            composite {
                composableParentThreshold(
                    root = composableRuleBased {
                        rule(isHealthCheck) { composableAlwaysOff() } // the first matching rule wins
                        rule(anySpan) { composableProbability(0.25) }
                    }
                )
            }
        }
    }
}
```

`composableRuleBased` checks its rules in order and uses the first one that matches. A span that
matches no rule isn't sampled, so finish with a rule that matches everything.

### Limits

Limits cap how much data a span or log record can hold. Data over a limit is dropped or truncated:

```kotlin configuration-limits
fun initWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    attributeLimits {
        attributeCountLimit = 64 // the default for every signal
        attributeValueLengthLimit = 1024
    }
    tracerProvider {
        spanLimits {
            attributeCountLimit = 128 // overrides the global limit for spans
            eventCountLimit = 32
            linkCountLimit = 32
            attributeCountPerEventLimit = 16
            attributeCountPerLinkLimit = 16
        }
    }
    loggerProvider {
        logRecordLimits { attributeValueLengthLimit = 4096 }
    }
}
```

`attributeLimits { }` applies to every signal. Limits set on `spanLimits { }` or `logRecordLimits { }`
override it for that specific signal. The global `attributeCountLimit` also applies to the attributes
of each span event and link, unless `attributeCountPerEventLimit` or `attributeCountPerLinkLimit` is set.

### Turning tracers and loggers off

A **tracer configurator** or **logger configurator** decides how each tracer or logger behaves,
based on its instrumentation scope. You can use this to turn off telemetry from 3rd party code
that's too noisy:

```kotlin configuration-configurators
private val disabledTracer = object : TracerConfig {
    override val enabled: Boolean = false
}
private val enabledTracer = object : TracerConfig {
    override val enabled: Boolean = true
}
private val warningsOnly = object : LoggerConfig {
    override val minimumSeverity: SeverityNumber = SeverityNumber.WARN
}
private val defaultLogger = object : LoggerConfig {}

fun initWithConfigurators(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Turn off one library's spans, matched by its instrumentation scope name.
        tracerConfigurator { scope ->
            when (scope.name) {
                "com.example.noisy" -> disabledTracer
                else -> enabledTracer
            }
        }
    }
    loggerProvider {
        loggerConfigurator { scope ->
            when {
                scope.name.startsWith("com.example.network") -> warningsOnly
                else -> defaultLogger
            }
        }
    }
}
```

The configurator is called when a tracer or logger is created and can be called often, so it
should return quickly. If it throws, the error is reported to the error handler and the tracer or
logger uses the default config, which is enabled.

### Error handling and disabling the SDK

The SDK never throws into your app. When something goes wrong, such as an invalid value or an
exporter that throws, it reports an `SdkError` to its **error handler** instead:

```kotlin configuration-diagnostics
fun initWithDiagnostics(isDebugBuild: Boolean, userOptedOut: Boolean): OpenTelemetrySdk = createOpenTelemetry {
    // Receives errors and misuse detected by the SDK. The default writes to the platform log.
    errorHandler { error -> println("otel: $error") }

    // The lowest severity passed to the handler. Defaults to INFO.
    logLevel(
        when {
            isDebugBuild -> SeverityNumber.DEBUG
            else -> SeverityNumber.WARN
        }
    )

    // When true, createOpenTelemetry returns a no-op SDK.
    disabled(userOptedOut)
}
```

- `errorHandler` replaces the default handler, which writes to the platform log. On the JVM, it
  uses `java.util.logging` with logger name `io.opentelemetry.kotlin`. The `INFO`, `WARNING`, and
  `ERROR` severities map to Java logging levels `INFO`, `WARNING`, and `SEVERE`. Your handler
  shouldn't throw.
- `logLevel` sets the lowest severity that reaches the handler. `INFO` and below pass
  everything, `WARN` passes warnings and errors, and `ERROR` and above pass errors only.
- `disabled(true)` makes `createOpenTelemetry` return a no-op SDK. This is useful for respecting
  a user's choice to opt out of telemetry.

### Everything else

These settings are covered in other guides or rarely needed:

- `propagator { }`: how context is sent between processes. See the
  [propagation guide](propagation.md#part-2-configuring-propagators).
- `context { }`: where the current context is stored. See the
  [context guide](context.md#installing-a-storage).
- `idGenerator { }`: generate your own trace and span IDs.
- `clock(...)`: the clock used to timestamp telemetry. Mostly useful in tests.
- `shutdownTimeout(...)`: how long `shutdown()` waits for processors to finish, in milliseconds.
  Defaults to 3000.
- `configFile(...)`: read a configuration file. See [Part 4](#part-4-configuration-files).

## Part 3: Environment variables

The SDK reads the standard
[environment variables](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/)
on the JVM, Linux and Node.js. It doesn't read them on Android, Apple platforms or in a browser.
The following variables are currently supported:

| Variable                                                                                   | Configures                                                                                           |
|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `OTEL_SDK_DISABLED`                                                                        | Whether the SDK is disabled.                                                                         |
| `OTEL_LOG_LEVEL`                                                                           | The lowest severity that reaches the error handler.                                                  |
| `OTEL_SERVICE_NAME`, `OTEL_RESOURCE_ATTRIBUTES`                                            | The resource.                                                                                        |
| `OTEL_TRACES_SAMPLER`, `OTEL_TRACES_SAMPLER_ARG`                                           | The sampler. `always_on`, `always_off`, `traceidratio` and their `parentbased_` forms are supported. |
| `OTEL_BSP_*`, `OTEL_BLRP_*`                                                                | The batch processor settings for spans and logs.                                                     |
| `OTEL_ATTRIBUTE_COUNT_LIMIT`, `OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT`                          | The global attribute limits.                                                                         |
| `OTEL_SPAN_*_LIMIT`, `OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT`, `OTEL_LINK_ATTRIBUTE_COUNT_LIMIT` | The span limits.                                                                                     |
| `OTEL_LOGRECORD_*_LIMIT`                                                                   | The log record limits.                                                                               |
| `OTEL_CONFIG_FILE`                                                                         | The path of a [configuration file](#part-4-configuration-files).                                     |

An invalid or unknown value is reported to the error handler as a warning and ignored.

Environment variable configuration is currently in active development.

## Part 4: Configuration files

A configuration file describes the SDK's configuration in YAML, using the
[declarative configuration](https://opentelemetry.io/docs/specs/otel/configuration/data-model/)
format. Using it requires adding an extra dependency:

```kotlin configuration-install-declarative
dependencies {
    implementation("io.opentelemetry.kotlin:config-declarative")
}
```

Then pass a reader to `configFile`:

```kotlin configuration-config-file
fun initFromConfigFile(): OpenTelemetrySdk = createOpenTelemetry {
    configFile(yamlConfigReader()) // reads the path in OTEL_CONFIG_FILE
}

fun initFromExplicitConfigFile(): OpenTelemetrySdk = createOpenTelemetry {
    configFile(yamlConfigReader(), path = "/etc/otel/otel.yaml")
}
```

Without a `path`, the SDK reads the file named by the `OTEL_CONFIG_FILE` environment variable. If
you set `OTEL_CONFIG_FILE` but don't call `configFile`, the SDK reports an error and ignores the
file. Files can be read on the JVM, Android and native platforms, but not on JS.

Here's an example file:

```yaml
file_format: "1.0"
log_level: info
resource:
  attributes:
    - name: service.name
      value: ${SERVICE_NAME:-checkout-service}
  attributes_list: "service.namespace=shop,service.version=2.4.1"
attribute_limits:
  attribute_count_limit: 64
tracer_provider:
  sampler:
    parent_based:
      root:
        always_on: {}
  limits:
    event_count_limit: 32
  processors:
    - batch:
        exporter:
          console: {}
logger_provider:
  limits:
    attribute_value_length_limit: 4096
  processors:
    - simple:
        exporter:
          console: {}
```

The SDK supports the following part of the format so far:

- `file_format`, `disabled` and `log_level`.
- `resource`, with `attributes`, `attributes_list` and `schema_url`.
- `attribute_limits`, and `limits` on `tracer_provider` and `logger_provider`.
- `tracer_provider.sampler`, with `always_on`, `always_off` and `parent_based`.
- `processors` on `tracer_provider` and `logger_provider`, with `simple` and `batch` processors.

YAML file configuration is currently in active development.

## Part 5: Precedence

When more than one way configures the same setting, the SDK uses this order, from lowest to
highest:

1. The SDK's defaults.
2. A configuration file, if there is one. Otherwise, environment variables.
3. The DSL.

Resource attributes are merged rather than replaced. From lowest to highest precedence, they come
from:

1. The SDK's default resource.
2. Resource detectors.
3. The configuration file or environment variables.
4. `serviceName` and `resource { }` at the top level of the DSL.
5. `resource { }` on a single signal.

## Part 6: Compat mode

`createCompatOpenTelemetry { }` takes the same DSL and reads the same environment variables and
files. The differences are:

- `context { }` is ignored and reported as a warning. Configure opentelemetry-java's
  `ContextStorage` instead.
- Processors and exporters run inside opentelemetry-java, which limits what a log record
  processor can change.

If you configure opentelemetry-java yourself, for example with its autoconfigure module, wrap that
SDK instead and none of this guide applies. See the [compat mode guide](compat.md).
