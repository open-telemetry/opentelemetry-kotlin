package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter

internal class FakeOtelJavaLogRecordExporter : OtelJavaLogRecordExporter {

    var flushCount = 0
    var shutdownCount = 0
    val exports: MutableList<OtelJavaLogRecordData> = mutableListOf()

    override fun export(logs: MutableCollection<OtelJavaLogRecordData>): OtelJavaCompletableResultCode {
        exports += logs
        return OtelJavaCompletableResultCode.ofSuccess()
    }

    override fun flush(): OtelJavaCompletableResultCode {
        flushCount += 1
        return OtelJavaCompletableResultCode.ofSuccess()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return OtelJavaCompletableResultCode.ofSuccess()
    }
}
