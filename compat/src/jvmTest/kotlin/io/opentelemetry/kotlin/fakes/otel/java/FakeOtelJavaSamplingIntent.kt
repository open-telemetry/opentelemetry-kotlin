package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingIntent
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import java.util.function.Function

internal class FakeOtelJavaSamplingIntent(
    private val threshold: Long = 0L,
    private val thresholdReliable: Boolean = true,
    private val attributes: OtelJavaAttributes = OtelJavaAttributes.empty(),
    private val traceStateUpdater: Function<OtelJavaTraceState, OtelJavaTraceState>? = Function.identity(),
) : OtelJavaSamplingIntent {

    override fun getThreshold(): Long = threshold

    override fun isThresholdReliable(): Boolean = thresholdReliable

    override fun getAttributes(): OtelJavaAttributes = attributes

    override fun getTraceStateUpdater(): Function<OtelJavaTraceState, OtelJavaTraceState>? = traceStateUpdater
}
