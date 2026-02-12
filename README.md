# <img src="https://opentelemetry.io/img/logos/opentelemetry-logo-nav.png" alt="OpenTelemetry Icon" width="45" height=""> OpenTelemetry Kotlin
[![codecov](https://codecov.io/github/open-telemetry/opentelemetry-kotlin/branch/main/graph/badge.svg?token=GQJYEOUSAU)](https://codecov.io/github/open-telemetry/opentelemetry-kotlin)

An implementation of the [OpenTelemetry specification](https://opentelemetry.io/docs/specs/otel/) as a Kotlin
Multiplatform Library.

This API operates in 2 modes:
1. Compatibility mode, where it acts as a façade for the [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java)
2. Regular mode, where it captures telemetry via a Kotlin Multiplatform (KMP) implementation

## Supported targets

The following targets are supported:

- Android (API >=21)
- JVM (JDK >= 8)
- iOS
- JavaScript

## Supported OTel APIs

- Tracing
- Logging

## Getting Started

### Regular mode

1. Add the following dependencies to your Android/Java project:

```
dependencies {
    val otelKotlinVersion = "0.1.0-alpha"
    implementation("io.opentelemetry.kotlin:core:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:implementation:$otelKotlinVersion")
}
```

2. Initialize the SDK:

```
val otelKotlin = createOpenTelemetry {
    // configure SDK here
}
```

3. Use the Kotlin API in your app

### Compatibility mode

Compatibility mode allows you to use a Kotlin API that uses the OpenTelemetry Java SDK under the hood to export telemetry.
This can be helpful if you already use the Java implementation or don't want to use the Kotlin implementation.

1. Add the following dependencies to your Android/Java project:

```
dependencies {
    val otelKotlinVersion = "0.1.0-alpha"
    implementation("io.opentelemetry.kotlin:core:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:compat:$otelKotlinVersion")
}
```

2. Wrap your existing [OTel Java](https://github.com/open-telemetry/opentelemetry-java) instance:

```
val otelJava = io.opentelemetry.sdk.OpenTelemetrySdk.builder().build()
val otelKotlin = otelJava.toOtelKotlinApi()

// or alternatively, create an instance that uses opentelemetry-java under the hood
val otelKotlin = createCompatOpenTelemetry {
    // configure SDK here
}
```

3. Use the Kotlin API instead of the Java API in your app

## Example usage

### Tracing API

```
val tracer = otelKotlin.tracerProvider.getTracer(
    name = "kotlin-example-app",
    version = "0.1.0"
)
tracer.createSpan("my_span")
```

### Logging API

```
val logger = otelKotlin.loggerProvider.getLogger("my_logger")
logger.log("Hello, World!")
```

### Example Apps

Example usage of the library can be found [here](examples).

## Feedback/bugs

Got feedback or found a bug? Please open a GitHub issue and we'll get back to you.

# Contributing

We are currently resource constrained and are actively seeking new contributors interested in working towards [approver](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#approver) / [maintainer](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#maintainer) roles.
In addition to the documentation for approver / maintainer roles and the [contributing](./CONTRIBUTING.md) guide, here are some additional notes on engaging:

- [Pull request](https://github.com/open-telemetry/opentelemetry-kotlin/pulls) reviews are equally or more helpful than code contributions. Comments and approvals are valuable with or without a formal project role. They're also a great forcing function to explore a fairly complex codebase.
- Attending the Special Interest Group (SIG) is a great way to get to know community members and learn about project priorities.
- Issues labeled [help wanted](https://github.com/open-telemetry/opentelemetry-kotlin/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22) are project priorities. Code contributions (or pull request reviews when a PR is linked) for these issues are particularly important.
- Triaging / responding to new issues and discussions is a great way to engage with the project.
- We are available in the [#otel-kotlin](https://cloud-native.slack.com/archives/C08NRCD4R4G) channel in the [CNCF Slack](https://slack.cncf.io/). Please join us there for further discussions.

## Maintainers

- [Jamie Lynch](https://github.com/fractalwrench), Embrace
- [Jason Plumb](https://github.com/breedx-splk), Splunk

For more information about the maintainer role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#maintainer).

## Approvers

- [Hanson Ho](https://github.com/bidetofevil), Embrace
- [Masaki Sugimoto](https://github.com/Msksgm), Henry, Inc
- [Francisco Prieto](https://github.com/priettt), Canary Technologies

For more information about the approver role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#approver).

### Thanks to all of our contributors!

<a href="https://github.com/open-telemetry/opentelemetry-kotlin/graphs/contributors">
  <img alt="Repo contributors" src="https://contrib.rocks/image?repo=open-telemetry/opentelemetry-kotlin" />
</a>