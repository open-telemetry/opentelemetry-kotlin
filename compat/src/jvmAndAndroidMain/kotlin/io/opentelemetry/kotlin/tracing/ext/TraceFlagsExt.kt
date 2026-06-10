package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.TraceFlags

internal fun TraceFlags.toOtelJavaTraceFlags(): OtelJavaTraceFlags {
    val traceFlagsHex = when {
        isRandom && isSampled -> "03"
        isRandom && !isSampled -> "02"
        !isRandom && isSampled -> "01"
        else -> "00"
    }
    return OtelJavaTraceFlags.fromHex(traceFlagsHex, 0)
}
