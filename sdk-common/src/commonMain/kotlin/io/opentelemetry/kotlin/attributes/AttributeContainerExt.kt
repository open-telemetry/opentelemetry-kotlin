package io.opentelemetry.kotlin.attributes

public fun AttributesMutator.setAttributes(container: AttributeContainer) {
    container.attributes.forEach { (key, value) ->
        when (value) {
            is Boolean -> setBooleanAttribute(key, value)
            is String -> setStringAttribute(key, value)
            is Long -> setLongAttribute(key, value)
            is Double -> setDoubleAttribute(key, value)
            is List<*> -> when (value.firstOrNull()) {
                is Boolean ->
                    @Suppress("UNCHECKED_CAST")
                    setBooleanListAttribute(key, value as List<Boolean>)
                is String ->
                    @Suppress("UNCHECKED_CAST")
                    setStringListAttribute(key, value as List<String>)
                is Long ->
                    @Suppress("UNCHECKED_CAST")
                    setLongListAttribute(key, value as List<Long>)
                is Double ->
                    @Suppress("UNCHECKED_CAST")
                    setDoubleListAttribute(key, value as List<Double>)
                else -> {}
            }
            else -> {}
        }
    }
}
