package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext

@OptIn(ExperimentalApi::class)
class FakeReadWriteLogRecord(
    override var timestamp: Long? = null,
    override var observedTimestamp: Long? = null,
    override var severityNumber: SeverityNumber? = SeverityNumber.UNKNOWN,
    override var severityText: String? = null,
    override var body: String? = null,
    override var eventName: String? = null,
    override var spanContext: SpanContext = FakeSpanContext.INVALID,
    override val attributes: Map<String, Any> = emptyMap(),
    override val resource: Resource = FakeResource(),
    override val instrumentationScopeInfo: InstrumentationScopeInfo =
        FakeInstrumentationScopeInfo(),
) : ReadWriteLogRecord {

    override fun setBooleanAttribute(key: String, value: Boolean) {
    }

    override fun setStringAttribute(key: String, value: String) {
    }

    override fun setLongAttribute(key: String, value: Long) {
    }

    override fun setDoubleAttribute(key: String, value: Double) {
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
    }
}
