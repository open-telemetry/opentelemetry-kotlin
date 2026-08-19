package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter

internal class FakeOtelJavaSpanExporter : OtelJavaSpanExporter {

    var flushCount = 0
    var shutdownCount = 0
    val exports: MutableList<OtelJavaSpanData> = mutableListOf()

    /**
     * Supplies the result of every operation, so tests can return results that complete
     * asynchronously (or not at all).
     */
    var nextResult: () -> OtelJavaCompletableResultCode = { OtelJavaCompletableResultCode.ofSuccess() }

    override fun export(logs: MutableCollection<OtelJavaSpanData>): OtelJavaCompletableResultCode {
        exports += logs
        return nextResult()
    }

    override fun flush(): OtelJavaCompletableResultCode {
        flushCount += 1
        return nextResult()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return nextResult()
    }
}
