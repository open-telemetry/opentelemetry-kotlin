package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ConfigDsl
import okio.Sink
import okio.blackholeSink
import okio.buffer

@ExperimentalApi
@ConfigDsl
public interface OtlpFileExporterConfigDsl {
    public var sink: Sink
}

internal class OtlpFileExporterConfig : OtlpFileExporterConfigDsl {
    override var sink: Sink = blackholeSink()
}

internal fun createOtlpFileExporter(
    block: OtlpFileExporterConfigDsl.() -> Unit,
): OtlpFileExporter {
    val config = OtlpFileExporterConfig().apply(block)
    return OtlpFileExporter(
        sink = config.sink.buffer()
    )
}
