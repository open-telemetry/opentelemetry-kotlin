package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainerImpl
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.LinkImpl
import io.opentelemetry.kotlin.tracing.SpanDataImpl
import io.opentelemetry.kotlin.tracing.SpanEventImpl
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * The single source of truth for span state. This is not exposed to consumers of the API - they
 * are presented with views such as [CreatedSpan], depending on which API call they make.
 */
@OptIn(ExperimentalApi::class)
internal class SpanModel(
    private val clock: Clock,
    private val processor: SpanProcessor?,
    name: String,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val resource: Resource,
    override val parent: SpanContext,
    override val spanContext: SpanContext,
    private val spanLimitConfig: SpanLimitConfig
) : ReadWriteSpan {

    private enum class State {
        STARTED,
        ENDING,
        ENDED
    }

    private val lock by lazy {
        ReentrantReadWriteLock()
    }

    private var state: State = State.STARTED

    override var name: String = name
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                if (isRecording()) {
                    field = value
                }
            }
        }

    override var status: StatusData = StatusData.Unset
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                if (isRecording()) {
                    field = value
                }
            }
        }

    override fun end() {
        endInternal(clock.now())
    }

    override fun end(timestamp: Long) {
        endInternal(timestamp)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun endInternal(timestamp: Long) {
        lock.write {
            if (state == State.STARTED) {
                state = State.ENDING
                endTimestamp = timestamp
                processor?.onEnding(ReadWriteSpanImpl(this))
                state = State.ENDED
                processor?.onEnd(ReadableSpanImpl(this))
            }
        }
    }

    override fun isRecording(): Boolean = state != State.ENDED

    private val eventsList = mutableListOf<EventData>()

    override val events: List<EventData>
        get() = lock.read {
            eventsList.toList()
        }

    private val linksList = mutableListOf<LinkData>()

    override val links: List<LinkData>
        get() = lock.read {
            linksList.toList()
        }

    override fun addLink(
        spanContext: SpanContext,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        lock.write {
            if (isRecording() && linksList.size < spanLimitConfig.linkCountLimit && !hasSpanContext(spanContext)) {
                val container = MutableAttributeContainerImpl(spanLimitConfig.attributeCountPerLinkLimit)
                if (attributes != null) {
                    attributes(container)
                }
                val link = LinkImpl(spanContext, container)
                linksList.add(link)
            }
        }
    }

    private fun hasSpanContext(spanContext: SpanContext): Boolean {
        return linksList.any {
            it.spanContext.traceId == spanContext.traceId && it.spanContext.spanId == spanContext.spanId
        }
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        lock.write {
            if (isRecording() && eventsList.size < spanLimitConfig.eventCountLimit) {
                val container = MutableAttributeContainerImpl(spanLimitConfig.attributeCountPerEventLimit)
                if (attributes != null) {
                    attributes(container)
                }
                val event = SpanEventImpl(name, timestamp ?: clock.now(), container)
                eventsList.add(event)
            }
        }
    }

    override fun toSpanData(): SpanData = SpanDataImpl(
        name,
        status,
        parent,
        spanContext,
        spanKind,
        startTimestamp,
        endTimestamp,
        attributes,
        events,
        links,
        resource,
        instrumentationScopeInfo,
        hasEnded
    )

    override var endTimestamp: Long? = null

    override val hasEnded: Boolean
        get() = state == State.ENDED

    private val attrs by lazy {
        MutableAttributeContainerImpl(spanLimitConfig.attributeCountLimit, mutableMapOf())
    }

    override val attributes: Map<String, Any>
        get() = lock.read {
            attrs.attributes
        }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        lock.write {
            if (isRecording()) {
                attrs.setBooleanAttribute(key, value)
            }
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        lock.write {
            if (isRecording()) {
                attrs.setStringAttribute(key, value)
            }
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        lock.write {
            if (isRecording()) {
                attrs.setLongAttribute(key, value)
            }
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        lock.write {
            if (isRecording()) {
                attrs.setDoubleAttribute(key, value)
            }
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        lock.write {
            if (isRecording()) {
                attrs.setBooleanListAttribute(key, value)
            }
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        lock.write {
            if (isRecording()) {
                attrs.setStringListAttribute(key, value)
            }
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        lock.write {
            if (isRecording()) {
                attrs.setLongListAttribute(key, value)
            }
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        lock.write {
            if (isRecording()) {
                attrs.setDoubleListAttribute(key, value)
            }
        }
    }
}
