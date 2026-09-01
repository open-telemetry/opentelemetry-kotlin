package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.logging.LogRecordDataImpl
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext

/**
 * The single source of truth for log record state. This is not exposed to consumers of the API - they
 * are presented with views such as [ReadWriteLogRecordImpl], or an immutable snapshot taken via
 * [toLogRecordData], depending on which API call they make.
 */
internal class LogRecordModel(
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    timestamp: Long,
    observedTimestamp: Long,
    body: Any?,
    eventName: String?,
    severityText: String?,
    severityNumber: SeverityNumber?,
    spanContext: SpanContext,
    logLimitConfig: LogLimitConfig,
    private val sdkErrorHandler: SdkErrorHandler,
) : ReadWriteLogRecord {

    private val lock = ReentrantReadWriteLock()

    private var timestampImpl: Long? = timestamp
    private var observedTimestampImpl: Long? = observedTimestamp
    private var severityNumberImpl: SeverityNumber? = severityNumber
    private var severityTextImpl: String? = severityText
    private var bodyImpl: Any? = body
    private var spanContextImpl: SpanContext = spanContext
    private var eventNameImpl: String? = eventName

    /**
     * Runs [action] behind the write lock. Input supplied by the host application must never escape
     * a public API method, so a failure is reported and swallowed.
     */
    private inline fun mutate(details: String, action: () -> Unit) {
        sdkErrorHandler.guard(details) {
            lock.write {
                action()
            }
        }
    }

    override var timestamp: Long?
        get() = lock.read { timestampImpl }
        set(value) = mutate("LogRecord.timestamp failed") {
            timestampImpl = value
        }

    override var observedTimestamp: Long?
        get() = lock.read { observedTimestampImpl }
        set(value) = mutate("LogRecord.observedTimestamp failed") {
            observedTimestampImpl = value
        }

    override var severityNumber: SeverityNumber?
        get() = lock.read { severityNumberImpl }
        set(value) = mutate("LogRecord.severityNumber failed") {
            severityNumberImpl = value
        }

    override var severityText: String?
        get() = lock.read { severityTextImpl }
        set(value) = mutate("LogRecord.severityText failed") {
            severityTextImpl = value
        }

    override var body: Any?
        get() = lock.read { bodyImpl }
        set(value) = mutate("LogRecord.body failed") {
            bodyImpl = value
        }

    override var spanContext: SpanContext
        get() = lock.read { spanContextImpl }
        set(value) = mutate("LogRecord.spanContext failed") {
            spanContextImpl = value
        }

    override var eventName: String?
        get() = lock.read { eventNameImpl }
        set(value) = mutate("LogRecord.eventName failed") {
            eventNameImpl = value
        }

    private val attrs by lazy {
        AttributesModel(
            attributeLimit = logLimitConfig.attributeCountLimit,
            attributeValueLengthLimit = logLimitConfig.attributeValueLengthLimit,
            attrs = mutableMapOf()
        )
    }

    override val attributes: Map<String, Any>
        get() = lock.read {
            attrs.attributes
        }

    override val droppedAttributesCount: Int
        get() = lock.read {
            attrs.droppedAttributesCount
        }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        mutate("LogRecord.setBooleanAttribute failed") {
            attrs.setBooleanAttribute(key, value)
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        mutate("LogRecord.setStringAttribute failed") {
            attrs.setStringAttribute(key, value)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        mutate("LogRecord.setLongAttribute failed") {
            attrs.setLongAttribute(key, value)
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        mutate("LogRecord.setDoubleAttribute failed") {
            attrs.setDoubleAttribute(key, value)
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        mutate("LogRecord.setBooleanListAttribute failed") {
            attrs.setBooleanListAttribute(key, value)
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        mutate("LogRecord.setStringListAttribute failed") {
            attrs.setStringListAttribute(key, value)
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        mutate("LogRecord.setLongListAttribute failed") {
            attrs.setLongListAttribute(key, value)
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        mutate("LogRecord.setDoubleListAttribute failed") {
            attrs.setDoubleListAttribute(key, value)
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        mutate("LogRecord.setByteArrayAttribute failed") {
            attrs.setByteArrayAttribute(key, value)
        }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        mutate("LogRecord.setAnyValueAttribute failed") {
            attrs.setAnyValueAttribute(key, value)
        }
    }

    /**
     * Takes the snapshot under a single read lock so that the returned [LogRecordData] is internally
     * consistent.
     */
    override fun toLogRecordData(): LogRecordData = lock.read {
        LogRecordDataImpl(
            timestampImpl,
            observedTimestampImpl,
            severityNumberImpl,
            severityTextImpl,
            bodyImpl,
            eventNameImpl,
            spanContextImpl,
            attrs.attributes,
            resource,
            instrumentationScopeInfo,
            attrs.droppedAttributesCount
        )
    }
}
