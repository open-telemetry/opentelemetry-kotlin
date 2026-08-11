package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.model.hex

internal fun TraceFlags.toOtelJavaTraceFlags(): OtelJavaTraceFlags =
    OtelJavaTraceFlags.fromHex(hex, 0)
