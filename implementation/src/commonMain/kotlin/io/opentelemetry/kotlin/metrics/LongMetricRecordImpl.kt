package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.metrics.export.LongMetricRecord

internal class LongMetricRecordImpl(
    override val name: String,
    override val type: String,
    override val value: Long,
    override val unit: String?,
    override val description: String?,
) : LongMetricRecord {

    private val lock by lazy {
        ReentrantReadWriteLock()
    }

    private val attrs by lazy {
        AttributesModel(
            attrs = mutableMapOf()
        )
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

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        lock.write {
            attrs.setByteArrayAttribute(key, value)
        }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        lock.write {
            attrs.setAnyValueAttribute(key, value)
        }
    }
}
