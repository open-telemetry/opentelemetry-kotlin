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
        return OtelJavaCompletableResultCode.ofSuccess()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return OtelJavaCompletableResultCode.ofSuccess()
    }
}
