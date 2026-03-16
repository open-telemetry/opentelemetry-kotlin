package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

class FakeReadWriteSpan(
    name: String = "fake_span",
    status: StatusData = StatusData.Unset,
    override val parent: SpanContext = FakeSpanContext.INVALID,
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val spanKind: SpanKind = SpanKind.INTERNAL,
    override val startTimestamp: Long = 0,
    override val events: List<SpanEventData> = emptyList(),
    override val links: List<SpanLinkData> = emptyList(),
    override val attributes: Map<String, Any> = emptyMap(),
    override val endTimestamp: Long? = 0,
    override val resource: Resource = FakeResource(),
    override val instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo(),
    override val hasEnded: Boolean = false
) : ReadWriteSpan {

    private var nameImpl: String = name
    override val name: String get() = nameImpl

    private var statusImpl: StatusData = status
    override val status: StatusData get() = statusImpl

    override fun setName(name: String) {
        nameImpl = name
    }

    override fun setStatus(status: StatusData) {
        statusImpl = status
    }

    override fun end() {
    }

    override fun end(timestamp: Long) {
    }

    override fun isRecording(): Boolean = true

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        throw UnsupportedOperationException()
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        throw UnsupportedOperationException()
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setStringAttribute(key: String, value: String) {
        throw UnsupportedOperationException()
    }

    override fun setLongAttribute(key: String, value: Long) {
        throw UnsupportedOperationException()
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        throw UnsupportedOperationException()
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        throw UnsupportedOperationException()
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        throw UnsupportedOperationException()
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        throw UnsupportedOperationException()
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        throw UnsupportedOperationException()
    }

    override fun toSpanData(): SpanData {
        throw UnsupportedOperationException()
    }

    override fun recordException(exception: Throwable, attributes: (AttributesMutator.() -> Unit)?) {
        throw UnsupportedOperationException()
    }
}
