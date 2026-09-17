package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.init.ResourceConfigDsl

/**
 * Captures the resource configured programmatically, and maps it onto a behavior.
 */
@ExperimentalApi
class ResourceConfigDslImpl : ResourceConfigDsl, BehaviorSupplier<ResourceBehavior> {

    private val attributes = ResourceAttributesMutator()
    private var schemaUrl: String? = null

    override var serviceName: String? = null

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        this.schemaUrl = schemaUrl
        this.attributes.attributes()
    }

    override fun resource(map: Map<String, Any>) {
        attributes.putAll(map)
    }

    override fun toBehavior(): ResourceBehavior = ResourceBehavior(
        serviceName = serviceName,
        schemaUrl = schemaUrl,
        attributes = attributes.toMap().takeUnless { it.isEmpty() },
    )
}

private class ResourceAttributesMutator : AttributesMutator {
    private val attributes = mutableMapOf<String, Any>()

    fun putAll(values: Map<String, Any>) {
        values.forEach { (key, value) ->
            when (value) {
                is AnyValue -> setAnyValueAttribute(key, value)
                is String -> setStringAttribute(key, value)
                is Boolean -> setBooleanAttribute(key, value)
                is Long -> setLongAttribute(key, value)
                is Number -> setNumericAttribute(key, value)
                is ByteArray -> setByteArrayAttribute(key, value)
                is Collection<*> -> setCollectionAttribute(key, value.toList())
                is Array<*> -> setCollectionAttribute(key, value.toList())
                else -> setStringAttribute(key, value.toString())
            }
        }
    }

    fun toMap(): Map<String, Any> = attributes.toMap()

    override fun setBooleanAttribute(key: String, value: Boolean) = set(key, value)
    override fun setStringAttribute(key: String, value: String) = set(key, value)
    override fun setLongAttribute(key: String, value: Long) = set(key, value)
    override fun setDoubleAttribute(key: String, value: Double) = set(key, value)
    override fun setBooleanListAttribute(key: String, value: List<Boolean>) = set(key, value.toList())
    override fun setStringListAttribute(key: String, value: List<String>) = set(key, value.toList())
    override fun setLongListAttribute(key: String, value: List<Long>) = set(key, value.toList())
    override fun setDoubleListAttribute(key: String, value: List<Double>) = set(key, value.toList())
    override fun setByteArrayAttribute(key: String, value: ByteArray) = set(key, value.copyOf())
    override fun setAnyValueAttribute(key: String, value: AnyValue) = set(key, value.copyValue())

    private fun setNumericAttribute(key: String, value: Number) {
        val doubleValue = value.toDouble()
        if (doubleValue.isFinite() && doubleValue == value.toLong().toDouble()) {
            setLongAttribute(key, value.toLong())
        } else {
            setDoubleAttribute(key, doubleValue)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCollectionAttribute(key: String, values: List<*>) {
        when {
            values.all { it is String } -> setStringListAttribute(key, values as List<String>)
            values.all { it is Boolean } -> setBooleanListAttribute(key, values as List<Boolean>)
            values.all { it is Long } -> setLongListAttribute(key, values as List<Long>)
            values.all { it is Number } -> {
                val numbers = values.filterIsInstance<Number>()
                if (numbers.all { it.toDouble().isFinite() && it.toDouble() == it.toLong().toDouble() }) {
                    setLongListAttribute(key, numbers.map { it.toLong() })
                } else {
                    setDoubleListAttribute(key, numbers.map { it.toDouble() })
                }
            }
            else -> setStringListAttribute(key, values.map { it.toString() })
        }
    }

    private fun AnyValue.copyValue(): AnyValue = when (this) {
        AnyValue.NullValue -> this
        is AnyValue.StringValue -> AnyValue.StringValue(value)
        is AnyValue.BoolValue -> AnyValue.BoolValue(value)
        is AnyValue.LongValue -> AnyValue.LongValue(value)
        is AnyValue.DoubleValue -> AnyValue.DoubleValue(value)
        is AnyValue.BytesValue -> AnyValue.BytesValue(value.copyOf())
        is AnyValue.ListValue -> AnyValue.ListValue(values.map { it.copyValue() })
        is AnyValue.MapValue -> AnyValue.MapValue(values.mapValues { it.value.copyValue() })
    }

    private fun set(key: String, value: Any) {
        attributes[key] = value
    }
}
