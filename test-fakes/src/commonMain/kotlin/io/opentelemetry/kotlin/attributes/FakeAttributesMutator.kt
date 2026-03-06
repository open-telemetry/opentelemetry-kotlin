package io.opentelemetry.kotlin.attributes
class FakeAttributesMutator(
    private val map: MutableMap<String, Any> = mutableMapOf()
) : AttributesMutator {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        map[key] = value
    }

    override fun setStringAttribute(key: String, value: String) {
        map[key] = value
    }

    override fun setLongAttribute(key: String, value: Long) {
        map[key] = value
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        map[key] = value
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        map[key] = value
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        map[key] = value
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        map[key] = value
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        map[key] = value
    }

    val attributes: Map<String, Any>
        get() = map.toMap()
}
