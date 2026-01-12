@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

class SpanDataImpl(
    override val name: String,
    override val status: StatusData,
    override val parent: SpanContext,
    override val spanContext: SpanContext,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    override val endTimestamp: Long?,
    override val attributes: Map<String, Any>,
    override val events: List<EventData>,
    override val links: List<LinkData>,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val hasEnded: Boolean,
) : SpanData
