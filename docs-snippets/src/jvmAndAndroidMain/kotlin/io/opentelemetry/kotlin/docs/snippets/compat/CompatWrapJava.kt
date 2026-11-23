package io.opentelemetry.kotlin.docs.snippets.compat

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.toOtelKotlinApi
import io.opentelemetry.api.OpenTelemetry as JavaOpenTelemetry

// region compat-wrap-java
// Wrap an opentelemetry-java SDK that your app already configures, e.g. with autoconfigure.
fun wrapJavaSdk(javaOtel: JavaOpenTelemetry): OpenTelemetrySdk = javaOtel.toOtelKotlinApi()
// end-region
