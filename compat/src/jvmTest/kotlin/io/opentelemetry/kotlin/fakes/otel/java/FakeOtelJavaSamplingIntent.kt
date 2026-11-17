package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingIntent
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import java.util.function.BiFunction

internal class FakeOtelJavaSamplingIntent(
    private val threshold: Long = 0L,
    private val thresholdReliable: Boolean = true,
    private val attributes: OtelJavaAttributes = OtelJavaAttributes.empty(),
    private val traceStateUpdater: BiFunction<OtelJavaTraceState, OtelJavaSamplingDecision, OtelJavaTraceState>? =
        BiFunction { state, _ -> state },
) : OtelJavaSamplingIntent {

    override fun getThreshold(): Long = threshold

    override fun isAdjustedCountReliable(): Boolean = thresholdReliable

    override fun getAttributes(): OtelJavaAttributes = attributes

    override fun getTraceStateUpdater(): BiFunction<OtelJavaTraceState, OtelJavaSamplingDecision, OtelJavaTraceState>? = traceStateUpdater
}
