package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ConfigDsl

@ExperimentalApi
@ConfigDsl
public interface OtlpFileExporterConfigDsl {

    /**
     * Receives exported telemetry, one OTLP JSON line per call (without a trailing newline).
     * Defaults to discarding all output.
     */
    public var sink: (String) -> Unit
}

internal class OtlpFileExporterConfig : OtlpFileExporterConfigDsl {
    override var sink: (String) -> Unit = {}
}

internal fun createOtlpFileExporter(
    block: (OtlpFileExporterConfigDsl.() -> Unit)?,
): OtlpFileExporter {
    val config = OtlpFileExporterConfig()
    block?.invoke(config)
    return OtlpFileExporter(
        sink = config.sink
    )
}
