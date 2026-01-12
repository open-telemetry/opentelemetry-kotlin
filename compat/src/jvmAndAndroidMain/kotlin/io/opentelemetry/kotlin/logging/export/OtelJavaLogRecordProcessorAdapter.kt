package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.context.toOtelKotlinContext

@OptIn(ExperimentalApi::class)
internal class OtelJavaLogRecordProcessorAdapter(
    private val impl: LogRecordProcessor
) : OtelJavaLogRecordProcessor {

    override fun onEmit(
        context: OtelJavaContext,
        logRecord: OtelJavaReadWriteLogRecord
    ) {
        impl.onEmit(ReadWriteLogRecordAdapter(logRecord), context.toOtelKotlinContext())
    }
}
