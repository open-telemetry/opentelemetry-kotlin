package io.opentelemetry.kotlin.attributes

public fun AttributesMutator.setAttributes(container: AttributeContainer) {
    container.attributes.forEach { (key, value) ->
        when (value) {
            is Boolean -> setBooleanAttribute(key, value)
            is String -> setStringAttribute(key, value)
            is Long -> setLongAttribute(key, value)
            is Double -> setDoubleAttribute(key, value)
            is Float -> setDoubleAttribute(key, value.toDouble())
            is Number -> setLongAttribute(key, value.toLong())
            is List<*> -> when (value.firstOrNull()) {
                is Boolean -> setBooleanListAttribute(key, value.filterIsInstance<Boolean>())
                is String -> setStringListAttribute(key, value.filterIsInstance<String>())
                is Long -> setLongListAttribute(key, value.filterIsInstance<Long>())
                is Double -> setDoubleListAttribute(key, value.filterIsInstance<Double>())
                is Float -> setDoubleListAttribute(key, value.filterIsInstance<Float>().map(Float::toDouble))
                is Number -> setLongListAttribute(key, value.filterIsInstance<Number>().map(Number::toLong))
                else -> {}
            }
            else -> {}
        }
    }
}
