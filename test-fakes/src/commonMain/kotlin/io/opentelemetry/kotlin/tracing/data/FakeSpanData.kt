package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

class FakeSpanData(
    override val name: String = "span",
    override val status: StatusData = StatusData.Ok,
    override val parent: SpanContext = FakeSpanContext.INVALID,
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val spanKind: SpanKind = SpanKind.INTERNAL,
    override val startTimestamp: Long = 1000,
    override val endTimestamp: Long = 2000,
    override val resource: Resource = FakeResource(),
    override val instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo(),
    override val attributes: Map<String, Any> = mapOf("key" to "value"),
    override val events: List<SpanEventData> = listOf(FakeSpanEventData()),
    override val links: List<SpanLinkData> = listOf(FakeSpanLinkData()),
    override val hasEnded: Boolean = true,
) : SpanData
