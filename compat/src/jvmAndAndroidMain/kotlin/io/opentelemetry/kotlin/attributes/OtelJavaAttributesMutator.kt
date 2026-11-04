package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributesBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaValue

/**
 * Destination for a typed opentelemetry-java attribute write.
 */
internal interface OtelJavaAttributeSink {
    fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T)
}

internal fun OtelJavaSpan.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        setAttribute(key, value)
    }
}

internal fun OtelJavaAttributesBuilder.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        this@attributeSink.put(key, value)
    }
}

internal fun OtelJavaReadWriteLogRecord.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        setAttribute(key, value)
    }
}

/**
 * The single mapping from Kotlin attribute setters to opentelemetry-java attribute keys. Bytes and [AnyValue] are
 * stored as `VALUE` because opentelemetry-java has no bytes attribute type. Lists are copied because the Java SDK
 * stores them by reference.
 */
internal class OtelJavaAttributesMutator(private val sink: OtelJavaAttributeSink) : AttributesMutator {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        sink.put(OtelJavaAttributeKey.booleanKey(key), value)
    }

    override fun setStringAttribute(key: String, value: String) {
        sink.put(OtelJavaAttributeKey.stringKey(key), value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        sink.put(OtelJavaAttributeKey.longKey(key), value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        sink.put(OtelJavaAttributeKey.doubleKey(key), value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        sink.put(OtelJavaAttributeKey.booleanArrayKey(key), value.toList())
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        sink.put(OtelJavaAttributeKey.stringArrayKey(key), value.toList())
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        sink.put(OtelJavaAttributeKey.longArrayKey(key), value.toList())
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        sink.put(OtelJavaAttributeKey.doubleArrayKey(key), value.toList())
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        sink.put(OtelJavaAttributeKey.valueKey(key), OtelJavaValue.of(value))
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        sink.put(OtelJavaAttributeKey.valueKey(key), value.toOtelJavaValue())
    }
}
