package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord

internal class FakeOtelJavaLogRecordProcessor : OtelJavaLogRecordProcessor {

    var flushCount = 0
    var shutdownCount = 0
    val exports: MutableList<OtelJavaReadWriteLogRecord> = mutableListOf()

    /**
     * Supplies the result of every operation, so tests can return results that complete
     * asynchronously (or not at all).
     */
    var nextResult: () -> OtelJavaCompletableResultCode = { OtelJavaCompletableResultCode.ofSuccess() }

    override fun onEmit(
        context: OtelJavaContext,
        logRecord: OtelJavaReadWriteLogRecord
    ) {
        exports += logRecord
    }

    override fun forceFlush(): OtelJavaCompletableResultCode? {
        flushCount += 1
        return nextResult()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return nextResult()
    }
}
