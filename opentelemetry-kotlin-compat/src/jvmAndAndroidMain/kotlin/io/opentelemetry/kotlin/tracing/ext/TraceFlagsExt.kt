package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@OptIn(ExperimentalApi::class)
internal fun TraceFlags.toOtelJavaTraceFlags(): OtelJavaTraceFlags {
    val sb = StringBuilder()
    sb.append(
        when {
            isRandom -> "1"
            else -> "0"
        }
    )
    sb.append(
        when {
            isSampled -> "1"
            else -> "0"
        }
    )
    return OtelJavaTraceFlags.fromHex(sb.toString(), 0)
}
