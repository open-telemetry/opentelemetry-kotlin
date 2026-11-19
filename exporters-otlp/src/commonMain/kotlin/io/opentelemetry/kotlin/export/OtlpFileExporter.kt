package io.opentelemetry.kotlin.export

internal class OtlpFileExporter(private val sink: (String) -> Unit = {}) {
    fun exportTelemetry(telemetry: List<String>): OtlpResponse {
        telemetry.forEach(sink)
        return OtlpResponse.Success
    }
}
