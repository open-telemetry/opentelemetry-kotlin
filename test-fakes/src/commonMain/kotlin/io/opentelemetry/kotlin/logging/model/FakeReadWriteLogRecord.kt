package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext

class FakeReadWriteLogRecord private constructor(
    override var timestamp: Long?,
    override var observedTimestamp: Long?,
    override var severityNumber: SeverityNumber?,
    override var severityText: String?,
    override var body: Any?,
    override var eventName: String?,
    override var spanContext: SpanContext,
    private val attrs: FakeAttributesMutator,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val droppedAttributesCount: Int,
) : ReadWriteLogRecord, AttributesMutator by attrs {

    constructor(
        timestamp: Long? = null,
        observedTimestamp: Long? = null,
        severityNumber: SeverityNumber? = SeverityNumber.UNKNOWN,
        severityText: String? = null,
        body: Any? = null,
        eventName: String? = null,
        spanContext: SpanContext = FakeSpanContext.INVALID,
        attributes: Map<String, Any> = emptyMap(),
        resource: Resource = FakeResource(),
        instrumentationScopeInfo: InstrumentationScopeInfo =
            FakeInstrumentationScopeInfo(),
        droppedAttributesCount: Int = 0,
    ) : this(
        timestamp,
        observedTimestamp,
        severityNumber,
        severityText,
        body,
        eventName,
        spanContext,
        FakeAttributesMutator(attributes.toMutableMap()),
        resource,
        instrumentationScopeInfo,
        droppedAttributesCount
    )

    override val attributes: Map<String, Any> get() = attrs.attributes

    override fun toLogRecordData(): LogRecordData = FakeLogRecordData(
        timestamp = timestamp,
        observedTimestamp = observedTimestamp,
        severityNumber = severityNumber,
        severityText = severityText,
        body = body,
        eventName = eventName,
        attributes = attributes,
        spanContext = spanContext,
        resource = resource,
        instrumentationScopeInfo = instrumentationScopeInfo,
        droppedAttributesCount = droppedAttributesCount
    )
}
