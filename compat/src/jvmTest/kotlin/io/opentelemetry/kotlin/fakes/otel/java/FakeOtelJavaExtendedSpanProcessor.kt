package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaExtendedSpanProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan

internal class FakeOtelJavaExtendedSpanProcessor(
    private val startRequired: Boolean = true,
    private val endRequired: Boolean = true,
    private val onEndingRequired: Boolean = true,
) : OtelJavaExtendedSpanProcessor {

    val startCalls: MutableList<OtelJavaReadWriteSpan> = mutableListOf()
    val endingCalls: MutableList<OtelJavaReadWriteSpan> = mutableListOf()
    val endCalls: MutableList<OtelJavaReadableSpan> = mutableListOf()

    override fun onStart(
        parentContext: OtelJavaContext,
        span: OtelJavaReadWriteSpan
    ) {
        startCalls += span
    }

    override fun onEnding(span: OtelJavaReadWriteSpan) {
        endingCalls += span
    }

    override fun onEnd(span: OtelJavaReadableSpan) {
        endCalls += span
    }

    override fun isStartRequired(): Boolean = startRequired
    override fun isEndRequired(): Boolean = endRequired
    override fun isOnEndingRequired(): Boolean = onEndingRequired
    override fun forceFlush(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
    override fun shutdown(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
}
