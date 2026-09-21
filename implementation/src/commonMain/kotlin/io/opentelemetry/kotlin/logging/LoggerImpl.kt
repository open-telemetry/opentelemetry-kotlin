package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.setExceptionAttributes
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.LogRecordModel
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecordImpl
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext

internal class LoggerImpl(
    private val clock: Clock,
    private val processor: LogRecordProcessor?,
    private val contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    private val key: InstrumentationScopeInfo,
    private val resource: Resource,
    private val logLimits: AttributeLimitsBehavior,
    private val shutdownState: ShutdownState,
    private val loggerConfig: LoggerConfig = LoggerConfigImpl(),
    private val sdkErrorHandler: SdkErrorHandler,
) : Logger {

    private val root = contextFactory.root()
    private val invalidSpanContext = spanContextFactory.invalid

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean =
        sdkErrorHandler.guardOrDefault(false, "Logger.enabled failed") {
            if (shutdownState.isShutdown || processor == null) {
                false
            } else {
                val ctx = context ?: contextFactory.implicit()
                when {
                    !allowedByConfig(severityNumber, spanContextFrom(ctx)) -> false
                    else -> sdkErrorHandler.guardOrDefault(true) {
                        processor.enabled(ctx, key, severityNumber, eventName)
                    }
                }
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
        processTelemetry(
            context = context,
            timestamp = timestamp,
            observedTimestamp = observedTimestamp,
            body = body,
            eventName = eventName,
            severityText = severityText,
            severityNumber = severityNumber,
            exception = exception,
            attributes = attributes
        )
    }

    private fun processTelemetry(
        context: Context?,
        timestamp: Long?,
        observedTimestamp: Long?,
        body: Any?,
        eventName: String?,
        severityText: String?,
        severityNumber: SeverityNumber?,
        exception: Throwable?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        sdkErrorHandler.guard("Logger.emit failed") {
            shutdownState.execute {
                val ctx = context ?: contextFactory.implicit()
                val spanContext = spanContextFrom(ctx)

                if (!allowedByConfig(severityNumber, spanContext)) {
                    return@execute
                }

                val now = clock.now()
                val log = LogRecordModel(
                    resource = resource,
                    instrumentationScopeInfo = key,
                    timestamp = timestamp ?: now,
                    observedTimestamp = observedTimestamp ?: now,
                    body = body,
                    severityText = severityText,
                    severityNumber = severityNumber ?: SeverityNumber.UNKNOWN,
                    spanContext = spanContext,
                    logLimits = logLimits,
                    eventName = eventName,
                    sdkErrorHandler = sdkErrorHandler,
                )
                if (exception != null) {
                    log.setExceptionAttributes(exception)
                }
                if (attributes != null) {
                    attributes(log)
                }
                sdkErrorHandler.guard {
                    processor?.onEmit(ReadWriteLogRecordImpl(log), ctx)
                }
            }
        }
    }

    private fun spanContextFrom(ctx: Context): SpanContext = when (ctx) {
        root -> invalidSpanContext
        else -> ctx.extractSpan().spanContext
    }

    /**
     * Whether [loggerConfig] permits a log record with the given severity and span context to be
     * processed.
     */
    private fun allowedByConfig(
        severityNumber: SeverityNumber?,
        spanContext: SpanContext,
    ): Boolean {
        val severity = severityNumber ?: SeverityNumber.UNKNOWN
        val belowMinimumSeverity = severity != SeverityNumber.UNKNOWN &&
            severity.severityNumber < loggerConfig.minimumSeverity.severityNumber
        if (belowMinimumSeverity) {
            return false
        }
        val unsampledTrace = spanContext.isValid && !spanContext.traceFlags.isSampled
        return !(loggerConfig.traceBased && unsampledTrace)
    }
}
