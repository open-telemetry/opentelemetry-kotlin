package io.opentelemetry.kotlin.export

import okio.BufferedSink

internal class OtlpFileExporter(private val sink: BufferedSink) {
    fun exportTelemetry(telemetry: List<String>): OtlpResponse {
        telemetry.forEach {
            sink.writeUtf8(it)
            sink.writeUtf8("\n")
        }
        sink.flush()
        return OtlpResponse.Success
    }
}
