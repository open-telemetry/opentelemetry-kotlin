package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.LogRecordModel
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecordImpl
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.resource.Resource

internal class LoggerImpl(
    private val clock: Clock,
    private val processor: LogRecordProcessor?,
    contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    spanFactory: SpanFactory,
    private val key: InstrumentationScopeInfo,
    private val resource: Resource,
    private val logLimitConfig: LogLimitConfig,
    private val shutdownState: ShutdownState,
) : Logger {

    private val contextFactory = contextFactory
    private val root = contextFactory.root()
    private val invalidSpanContext = spanContextFactory.invalid
    private val spanFactory = spanFactory

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean =
        if (shutdownState.isShutdown || processor == null) {
            false
        } else {
            val ctx = context ?: contextFactory.implicit()
            processor.enabled(ctx, key, severityNumber, eventName)
        }

    override fun emit(
        body: String?,
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
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
            attributes = attributes
        )
    }

    private fun processTelemetry(
        context: Context?,
        timestamp: Long?,
        observedTimestamp: Long?,
        body: String?,
        eventName: String?,
        severityText: String?,
        severityNumber: SeverityNumber?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        shutdownState.execute {
            val ctx = context ?: contextFactory.implicit()
            val spanContext = when (ctx) {
                root -> invalidSpanContext
                else -> spanFactory.fromContext(ctx).spanContext
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
                logLimitConfig = logLimitConfig,
                eventName = eventName,
            )
            if (attributes != null) {
                attributes(log)
            }
            processor?.onEmit(ReadWriteLogRecordImpl(log), ctx)
        }
    }
}
