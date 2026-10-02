package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.export.telemetryExceptionHandler
import io.opentelemetry.kotlin.launchAsCompletableResultCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class OtelJavaLogRecordProcessorAdapter(
    private val impl: LogRecordProcessor,
    private val sdkErrorHandler: SdkErrorHandler,
) : OtelJavaLogRecordProcessor {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + telemetryExceptionHandler("LogRecordProcessor", sdkErrorHandler)
    )

    override fun onEmit(
        context: OtelJavaContext,
        logRecord: OtelJavaReadWriteLogRecord
    ) {
        sdkErrorHandler.guard("LogRecordProcessor.onEmit failed") {
            impl.onEmit(ReadWriteLogRecordAdapter(logRecord), context.toOtelKotlinContext())
        }
    }

    override fun forceFlush(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordProcessor.forceFlush") {
            impl.forceFlush()
        }

    override fun shutdown(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordProcessor.shutdown") {
            impl.shutdown()
        }.whenComplete { scope.cancel() }
}
