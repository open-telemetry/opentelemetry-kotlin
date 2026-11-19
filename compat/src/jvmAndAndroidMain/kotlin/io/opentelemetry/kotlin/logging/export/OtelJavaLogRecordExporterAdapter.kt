package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.telemetryExceptionHandler
import io.opentelemetry.kotlin.launchAsCompletableResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordDataAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class OtelJavaLogRecordExporterAdapter(
    private val impl: LogRecordExporter,
    private val sdkErrorHandler: SdkErrorHandler,
) : OtelJavaLogRecordExporter {

    private val scope by lazy {
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default +
                telemetryExceptionHandler("LogRecordExporter", sdkErrorHandler)
        )
    }

    override fun export(logs: MutableCollection<OtelJavaLogRecordData>): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordExporter.export") {
            impl.export(logs.map(::LogRecordDataAdapter))
        }

    override fun flush(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordExporter.forceFlush") {
            impl.forceFlush()
        }

    override fun shutdown(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "LogRecordExporter.shutdown") {
            impl.shutdown()
        }.whenComplete { scope.cancel() }
}
