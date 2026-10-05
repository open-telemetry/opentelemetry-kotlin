package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ConfigDsl
import okio.Sink
import okio.buffer

@ExperimentalApi
@ConfigDsl
public interface OtlpFileExporterConfigDsl {
    public val sink: Sink
}

internal class OtlpFileExporterConfig : OtlpFileExporterConfigDsl {
    override val sink: Sink = TODO("this initializer could be an expect fun?")
}

internal fun createOtlpFileExporter(
    block: OtlpFileExporterConfigDsl.() -> Unit,
): OtlpFileExporter {
    val config = OtlpFileExporterConfig().apply(block)
    return OtlpFileExporter(
        sink = config.sink.buffer()
    )
}
