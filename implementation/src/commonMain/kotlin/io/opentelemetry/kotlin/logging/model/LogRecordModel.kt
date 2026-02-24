package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.model.SpanContext

/**
 * The single source of truth for log record state. This is not exposed to consumers of the API - they
 * are presented with views such as [ReadableLogRecordImpl], depending on which API call they make.
 */
@OptIn(ExperimentalApi::class)
internal class LogRecordModel(
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    timestamp: Long,
    observedTimestamp: Long,
    body: String?,
    eventName: String?,
    severityText: String?,
    severityNumber: SeverityNumber?,
    override val spanContext: SpanContext,
    logLimitConfig: LogLimitConfig,
) : ReadWriteLogRecord {

    private val lock by lazy {
        ReentrantReadWriteLock()
    }

    override var timestamp: Long? = timestamp
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    override var observedTimestamp: Long? = observedTimestamp
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    override var severityNumber: SeverityNumber? = severityNumber
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    override var severityText: String? = severityText
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    override var body: String? = body
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    override var eventName: String? = eventName
        get() = lock.read {
            field
        }
        set(value) {
            lock.write {
                field = value
            }
        }

    private val attrs by lazy {
        AttributesModel(logLimitConfig.attributeCountLimit, mutableMapOf())
    }

    override val attributes: Map<String, Any>
        get() = lock.read {
            attrs.attributes.toMap()
        }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        lock.write {
            attrs.setBooleanAttribute(key, value)
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        lock.write {
            attrs.setStringAttribute(key, value)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        lock.write {
            attrs.setLongAttribute(key, value)
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        lock.write {
            attrs.setDoubleAttribute(key, value)
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        lock.write {
            attrs.setBooleanListAttribute(key, value)
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        lock.write {
            attrs.setStringListAttribute(key, value)
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        lock.write {
            attrs.setLongListAttribute(key, value)
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        lock.write {
            attrs.setDoubleListAttribute(key, value)
        }
    }
}
