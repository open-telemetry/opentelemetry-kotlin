package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor

internal class FakeOtelJavaSpanProcessor : OtelJavaSpanProcessor {

    var flushCount = 0
    var shutdownCount = 0
    val startCalls: MutableList<OtelJavaReadWriteSpan> = mutableListOf()
    val endCalls: MutableList<OtelJavaReadableSpan> = mutableListOf()

    /**
     * Supplies the result of every operation, so tests can return results that complete
     * asynchronously (or not at all).
     */
    var nextResult: () -> OtelJavaCompletableResultCode = { OtelJavaCompletableResultCode.ofSuccess() }

    override fun onStart(
        parentContext: OtelJavaContext,
        span: OtelJavaReadWriteSpan
    ) {
        startCalls += span
    }

    override fun onEnd(span: OtelJavaReadableSpan) {
        endCalls += span
    }

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true

    override fun forceFlush(): OtelJavaCompletableResultCode? {
        flushCount += 1
        return nextResult()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return nextResult()
    }
}
