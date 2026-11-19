package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.compositeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.SessionAttributes

// region logging-custom-processor
@OptIn(IncubatingApi::class)
class SessionIdProcessor(private val sessionId: () -> String) : LogRecordProcessor {
    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        log.setStringAttribute(SessionAttributes.SESSION_ID, sessionId())
    }

    override suspend fun forceFlush() = OperationResultCode.Success
    override suspend fun shutdown() = OperationResultCode.Success
}

fun initLoggingWithProcessor(sessionId: () -> String): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        export {
            compositeLogRecordProcessor(
                SessionIdProcessor(sessionId), // runs first, so the exported log has the attribute
                batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "http://localhost:4318" }),
            )
        }
    }
}
// end-region
