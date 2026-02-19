package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags

@OptIn(ExperimentalApi::class)
internal class TraceFlagsAdapter(
    traceFlags: OtelJavaTraceFlags
) : TraceFlags {

    override val isSampled: Boolean = traceFlags.isSampled

    // verify if the second bit (random flag) is set, using bitwise AND
    override val isRandom: Boolean = traceFlags.asByte().toInt() and 0b00000010 != 0
}
