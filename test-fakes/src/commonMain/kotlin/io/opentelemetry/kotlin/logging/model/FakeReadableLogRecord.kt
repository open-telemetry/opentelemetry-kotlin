package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeSpanContext

@OptIn(ExperimentalApi::class)
class FakeReadableLogRecord(
    override val timestamp: Long? = 1000,
    override val observedTimestamp: Long? = 2000,
    override val severityNumber: SeverityNumber? = SeverityNumber.WARN,
    override val severityText: String? = "warning",
    override val body: String? = "Hello, World!",
    override val eventName: String? = null,
    override val attributes: Map<String, Any> = mapOf("key" to "value"),
    override val spanContext: FakeSpanContext = FakeSpanContext.INVALID,
    override val resource: Resource = FakeResource(),
    override val instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo()
) : ReadableLogRecord
