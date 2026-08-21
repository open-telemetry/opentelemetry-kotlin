package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanDataImpl
import io.opentelemetry.kotlin.tracing.SpanEventImpl
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.frozenCopy
import io.opentelemetry.kotlin.tracing.toSnapshot

/**
 * The single source of truth for span state. This is not exposed to consumers of the API - they
 * are presented with views such as [CreatedSpan], or an immutable snapshot once the
 * span has ended, depending on which API call they make.
 */
internal class SpanModel(
    private val clock: Clock,
    private val processor: SpanProcessor?,
    name: String,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val resource: Resource,
    override val parent: SpanContext,
    spanContext: SpanContext,
    private val spanLimitConfig: SpanLimitConfig,
    initialLinks: List<SpanLink>,
    private val initialDroppedAttributesCount: Int = 0,
    initialDroppedLinksCount: Int = 0,
    private val sdkErrorHandler: SdkErrorHandler
) : ReadWriteSpan, SpanCreationAction {

    private enum class State {
        STARTED,
        ENDING,
        ENDED
    }

    private val lock = ReentrantReadWriteLock()

    private var state: State = State.STARTED

    private var spanContextImpl: SpanContext = spanContext
    private var nameImpl: String = name
    private var statusImpl: StatusData = StatusData.Unset

    override val name: String
        get() = lock.read { nameImpl }

    override val status: StatusData
        get() = lock.read { statusImpl }

    override var spanContext: SpanContext
        get() = lock.read { spanContextImpl }
        set(value) = mutate("Span.spanContext failed") {
            spanContextImpl = value
        }

    /**
     * Runs [action] behind the write lock if the span is still recording. Input supplied by the
     * host application must never escape a public API method, so a failure is reported and
     * swallowed.
     */
    private inline fun mutate(details: String, action: () -> Unit) {
        sdkErrorHandler.guard(details) {
            lock.write {
                if (isRecordingInternal()) {
                    action()
                }
            }
        }
    }

    override fun setName(name: String) {
        mutate("Span.setName failed") {
            nameImpl = name
        }
    }

    override fun setStatus(status: StatusData) {
        if (status is StatusData.Unset) return
        mutate("Span.setStatus failed") {
            if (statusImpl !is StatusData.Ok) {
                statusImpl = status
            }
        }
    }

    override fun end() {
        // a broken clock must not stop the span ending, so fall back to a zero timestamp
        endInternal(
            sdkErrorHandler.guardOrDefault(0L, "Clock.now failed during Span.end") {
                clock.now()
            }
        )
    }

    override fun end(timestamp: Long) {
        endInternal(timestamp)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun endInternal(timestamp: Long) {
        sdkErrorHandler.guard("Span.end failed") {
            lock.write {
                if (state == State.STARTED) {
                    state = State.ENDING
                    endTimestampImpl = timestamp
                    sdkErrorHandler.guard {
                        processor?.onEnding(ReadWriteSpanImpl(this))
                    }
                    state = State.ENDED
                    sdkErrorHandler.guard {
                        // take a snapshot so that processors retain plain data rather than this
                        // model, releasing its properties once the span
                        // has ended. Only built if a processor needs it.
                        processor?.takeIf(SpanProcessor::isEndRequired)?.onEnd(toSpanData())
                    }
                }
            }
        }
    }

    override fun isRecording(): Boolean =
        sdkErrorHandler.guardOrDefault(false, "Span.isRecording failed") {
            lock.read { isRecordingInternal() }
        }

    /**
     * Reads [state] without acquiring [lock]. Callers must already hold [lock].
     */
    private fun isRecordingInternal(): Boolean = state != State.ENDED

    private val eventsList = mutableListOf<SpanEventData>()

    private var droppedEventsCountImpl = 0

    override val events: List<SpanEventData>
        get() = lock.read {
            eventsList.toList()
        }

    override val droppedEventsCount: Int
        get() = lock.read {
            droppedEventsCountImpl
        }

    private val linksList = initialLinks.toMutableList<SpanLinkData>()

    private var droppedLinksCountImpl = initialDroppedLinksCount

    override val links: List<SpanLinkData>
        get() = lock.read {
            linksList.toList()
        }

    override val droppedLinksCount: Int
        get() = lock.read {
            droppedLinksCountImpl
        }

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        mutate("Span.addLink failed") {
            if (linksList.size < spanLimitConfig.linkCountLimit) {
                val link = buildSpanLink(spanContext, attributes, spanLimitConfig)
                linksList.add(link)
            } else {
                droppedLinksCountImpl++
            }
        }
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        mutate("Span.addEvent failed") {
            if (eventsList.size < spanLimitConfig.eventCountLimit) {
                val container = AttributesModel(
                    attributeLimit = spanLimitConfig.attributeCountPerEventLimit,
                    attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
                )
                if (attributes != null) {
                    attributes(container)
                }
                val event = SpanEventImpl(name, timestamp ?: clock.now(), container)
                eventsList.add(event)
            } else {
                droppedEventsCountImpl++
            }
        }
    }

    /**
     * Takes the snapshot under a single read lock so that the returned [ReadableSpan] is internally
     * consistent.
     */
    override fun toSpanData(): ReadableSpan = lock.read {
        SpanDataImpl(
            nameImpl,
            statusImpl,
            parent,
            spanContextImpl,
            spanKind,
            startTimestamp,
            endTimestampImpl,
            attrs.attributes.frozenCopy(),
            eventsList.map { it.toSnapshot() },
            droppedEventsCountImpl,
            linksList.map { it.toSnapshot() },
            droppedLinksCountImpl,
            resource,
            instrumentationScopeInfo,
            state == State.ENDED,
            attrs.droppedAttributesCount + initialDroppedAttributesCount
        )
    }

    private var endTimestampImpl: Long? = null

    override val endTimestamp: Long?
        get() = lock.read { endTimestampImpl }

    override val hasEnded: Boolean
        get() = lock.read { state == State.ENDED }

    private val attrs by lazy {
        AttributesModel(
            attributeLimit = spanLimitConfig.attributeCountLimit,
            attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit,
            attrs = mutableMapOf()
        )
    }

    override val attributes: Map<String, Any>
        get() = lock.read {
            attrs.attributes
        }

    override val droppedAttributesCount: Int
        get() = lock.read {
            attrs.droppedAttributesCount + initialDroppedAttributesCount
        }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        mutate("Span.setBooleanAttribute failed") {
            attrs.setBooleanAttribute(key, value)
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        mutate("Span.setStringAttribute failed") {
            attrs.setStringAttribute(key, value)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        mutate("Span.setLongAttribute failed") {
            attrs.setLongAttribute(key, value)
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        mutate("Span.setDoubleAttribute failed") {
            attrs.setDoubleAttribute(key, value)
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        mutate("Span.setBooleanListAttribute failed") {
            attrs.setBooleanListAttribute(key, value)
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        mutate("Span.setStringListAttribute failed") {
            attrs.setStringListAttribute(key, value)
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        mutate("Span.setLongListAttribute failed") {
            attrs.setLongListAttribute(key, value)
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        mutate("Span.setDoubleListAttribute failed") {
            attrs.setDoubleListAttribute(key, value)
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        mutate("Span.setByteArrayAttribute failed") {
            attrs.setByteArrayAttribute(key, value)
        }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        mutate("Span.setAnyValueAttribute failed") {
            attrs.setAnyValueAttribute(key, value)
        }
    }
}
