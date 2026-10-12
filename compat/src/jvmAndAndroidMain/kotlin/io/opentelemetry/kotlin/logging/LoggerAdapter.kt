package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaValueType
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setExceptionAttributes
import io.opentelemetry.kotlin.attributes.toOtelJavaValue
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import java.util.concurrent.TimeUnit

@ExperimentalApi
internal class LoggerAdapter(
    private val impl: OtelJavaLogger,
    private val sdkErrorHandler: SdkErrorHandler,
) : Logger {

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = sdkErrorHandler.sdkGuardOrDefault(false, "Logger.enabled failed") {
        // eventName has no equivalent in opentelemetry-java, so it is not taken into account
        val severity = (severityNumber ?: SeverityNumber.UNKNOWN).toOtelJavaSeverityNumber()
        when (context) {
            null -> impl.isEnabled(severity)
            else -> impl.isEnabled(severity, context.toOtelJavaContext())
        }
    }

    override fun emit(
        body: Any?,
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        exception: Throwable?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        sdkErrorHandler.sdkGuard("Logger.emit failed") {
            processTelemetry(
                eventName = eventName,
                body = body,
                timestamp = timestamp,
                observedTimestamp = observedTimestamp,
                context = context,
                severityNumber = severityNumber,
                severityText = severityText,
                exception = exception,
                attributes = attributes
            )
        }
    }

    private fun processTelemetry(
        eventName: String?,
        body: Any?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        exception: Throwable?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val builder = impl.logRecordBuilder()

        val otelJavaBody = sdkErrorHandler.guardOrDefault(null, "LogRecord.body failed") {
            body?.toOtelJavaValue()?.takeIf { it.type != OtelJavaValueType.EMPTY }
        }
        if (otelJavaBody != null) {
            builder.setBody(otelJavaBody)
        }
        if (eventName != null) {
            builder.setEventName(eventName)
        }
        if (timestamp != null && timestamp > 0) {
            builder.setTimestamp(timestamp, TimeUnit.NANOSECONDS)
        }
        if (observedTimestamp != null && observedTimestamp > 0) {
            builder.setObservedTimestamp(observedTimestamp, TimeUnit.NANOSECONDS)
        }
        if (context != null) {
            builder.setContext(context.toOtelJavaContext())
        }
        if (severityNumber != null) {
            builder.setSeverity(severityNumber.toOtelJavaSeverityNumber())
        }
        if (severityText != null) {
            builder.setSeverityText(severityText)
        }

        val container = CompatAttributesModel()
        if (exception != null) {
            userCode { container.setExceptionAttributes(exception) }
        }
        if (attributes != null) {
            userCode { attributes(container) }
        }
        if (exception != null || attributes != null) {
            builder.setAllAttributes(container.otelJavaAttributes())
        }
        builder.emit()
    }
}
