package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.COMPAT_DEFAULT_TIMEOUT_MS
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
    private val timeoutMs: Long = COMPAT_DEFAULT_TIMEOUT_MS,
) : OtelJavaLogRecordProcessor {

    private val unsupportedWrites = UnsupportedLogRecordWriteReporter(sdkErrorHandler)

    private val scope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default + telemetryExceptionHandler("LogRecordProcessor", sdkErrorHandler))
    }

    override fun onEmit(
        context: OtelJavaContext,
        logRecord: OtelJavaReadWriteLogRecord
    ) {
        sdkErrorHandler.guard("LogRecordProcessor.onEmit failed") {
            impl.onEmit(ReadWriteLogRecordAdapter(logRecord, unsupportedWrites), context.toOtelKotlinContext())
        }
    }

    override fun forceFlush(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordProcessor.forceFlush", timeoutMs) {
            impl.forceFlush()
        }

    override fun shutdown(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordProcessor.shutdown", timeoutMs) {
            impl.shutdown()
        }.whenComplete { scope.cancel() }
}
