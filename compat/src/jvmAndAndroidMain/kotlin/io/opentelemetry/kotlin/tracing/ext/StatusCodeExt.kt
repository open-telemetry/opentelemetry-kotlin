package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.tracing.StatusCode

internal fun StatusCode.toOtelJavaStatusCode(): OtelJavaStatusCode = when (this) {
    StatusCode.UNSET -> OtelJavaStatusCode.UNSET
    StatusCode.OK -> OtelJavaStatusCode.OK
    StatusCode.ERROR -> OtelJavaStatusCode.ERROR
}
