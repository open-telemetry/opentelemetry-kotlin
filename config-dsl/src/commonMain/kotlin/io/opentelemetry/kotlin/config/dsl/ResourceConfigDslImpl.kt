package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.setAttributes
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
        attributes.setAttributes(map)
    }

    override fun toBehavior(): ResourceBehavior = ResourceBehavior(
        serviceName = serviceName,
        schemaUrl = schemaUrl,
        attributes = attributes.toMap().takeUnless { it.isEmpty() },
    )
}

private class ResourceAttributesMutator : AttributesMutator {
    private val attributes = mutableMapOf<String, Any>()

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
