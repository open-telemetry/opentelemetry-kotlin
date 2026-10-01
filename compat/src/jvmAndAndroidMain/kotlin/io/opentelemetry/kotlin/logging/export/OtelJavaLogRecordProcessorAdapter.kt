package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard

internal class OtelJavaLogRecordProcessorAdapter(
    private val impl: LogRecordProcessor,
    private val sdkErrorHandler: SdkErrorHandler,
) : OtelJavaLogRecordProcessor {

    override fun onEmit(
        context: OtelJavaContext,
        logRecord: OtelJavaReadWriteLogRecord
    ) {
        sdkErrorHandler.guard("LogRecordProcessor.onEmit failed") {
            impl.onEmit(ReadWriteLogRecordAdapter(logRecord), context.toOtelKotlinContext())
        }
    }
}
