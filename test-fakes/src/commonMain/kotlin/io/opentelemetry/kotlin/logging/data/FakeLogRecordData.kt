package io.opentelemetry.kotlin.logging.data

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext

class FakeLogRecordData(
    override val timestamp: Long? = 1000,
    override val observedTimestamp: Long? = 2000,
    override val severityNumber: SeverityNumber? = SeverityNumber.WARN,
    override val severityText: String? = "warning",
    override val body: Any? = "Hello, World!",
    override val eventName: String? = null,
    override val attributes: Map<String, Any> = mapOf("key" to "value"),
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val resource: Resource = FakeResource(),
    override val instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo(),
    override val droppedAttributesCount: Int = 0
) : LogRecordData
