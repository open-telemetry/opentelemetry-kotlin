package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.telemetryExceptionHandler
import io.opentelemetry.kotlin.launchAsCompletableResultCode
import io.opentelemetry.kotlin.tracing.data.SpanDataAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class OtelJavaSpanExporterAdapter(
    private val impl: SpanExporter,
    private val sdkErrorHandler: SdkErrorHandler,
) : OtelJavaSpanExporter {

    private val scope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default + telemetryExceptionHandler("SpanExporter", sdkErrorHandler))
    }

    override fun export(spans: MutableCollection<OtelJavaSpanData>): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "SpanExporter.export") {
            impl.export(spans.map(::SpanDataAdapter))
        }

    override fun flush(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "SpanExporter.forceFlush") {
            impl.forceFlush()
        }

    override fun shutdown(): OtelJavaCompletableResultCode =
        scope.launchAsCompletableResultCode(sdkErrorHandler, "SpanExporter.shutdown") {
            impl.shutdown()
        }.whenComplete { scope.cancel() }
}
