package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext

/**
 * An immutable snapshot of a log record's state.
 */
class LogRecordDataImpl(
    override val timestamp: Long?,
    override val observedTimestamp: Long?,
    override val severityNumber: SeverityNumber?,
    override val severityText: String?,
    override val body: Any?,
    override val eventName: String?,
    override val spanContext: SpanContext,
    override val attributes: Map<String, Any>,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val droppedAttributesCount: Int = 0,
) : LogRecordData
