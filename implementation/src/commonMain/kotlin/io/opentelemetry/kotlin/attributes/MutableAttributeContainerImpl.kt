package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.threadSafeMap

@OptIn(ExperimentalApi::class)
@ThreadSafe
internal class MutableAttributeContainerImpl(
    private val attributeLimit: Int = DEFAULT_ATTRIBUTE_LIMIT,
    private val attrs: MutableMap<String, Any> = threadSafeMap()
) : MutableAttributeContainer {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    private fun canAddAttribute(key: String): Boolean = attrs.size < attributeLimit || attrs.contains(key)
}

internal const val DEFAULT_ATTRIBUTE_LIMIT: Int = 128
