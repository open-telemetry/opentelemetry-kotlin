package io.opentelemetry.kotlin.attributes

/**
 * Holds attributes for telemetry that frequently has none (e.g. span events and links). The
 * backing [AttributesModel] is only allocated on the first write, so telemetry created without
 * attributes doesn't allocate a container. [attributeLimit] and [attributeValueLengthLimit]
 * apply to every write, whenever it happens.
 */
internal abstract class LazyAttributesHolder(
    private val attributeLimit: Int,
    private val attributeValueLengthLimit: Int,
) : AttributesMutator, AttributeContainer {

    private var attrs: AttributesModel? = null

    val droppedAttributesCount: Int
        get() = attrs?.droppedAttributesCount ?: 0

    override val attributes: Map<String, Any>
        get() = attrs?.attributes ?: emptyMap()

    private fun model(): AttributesModel = attrs ?: AttributesModel(
        attributeLimit = attributeLimit,
        attributeValueLengthLimit = attributeValueLengthLimit,
    ).also { attrs = it }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        model().setBooleanAttribute(key, value)
    }

    override fun setStringAttribute(key: String, value: String) {
        model().setStringAttribute(key, value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        model().setLongAttribute(key, value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        model().setDoubleAttribute(key, value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        model().setBooleanListAttribute(key, value)
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        model().setStringListAttribute(key, value)
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        model().setLongListAttribute(key, value)
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        model().setDoubleListAttribute(key, value)
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        model().setByteArrayAttribute(key, value)
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        model().setAnyValueAttribute(key, value)
    }
}
