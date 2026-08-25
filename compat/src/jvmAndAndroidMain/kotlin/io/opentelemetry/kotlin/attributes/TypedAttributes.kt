package io.opentelemetry.kotlin.attributes

/**
 * Copies an attribute map onto an [AttributesMutator], preserving the double-ness of its values.
 *
 * The common [setAttributes] widens any whole-valued number to a long, because Kotlin/JS cannot
 * tell an [Int] from a [Double] at runtime. Java OTel attributes are statically typed, so that
 * ambiguity does not exist here and a double must survive the round trip as a double. Doubles are
 * therefore dispatched here and everything else is delegated to the common implementation.
 */
internal fun AttributesMutator.setTypedAttributes(attributes: Map<String, Any>) {
    attributes.forEach { setTypedAttribute(it.key, it.value) }
}

internal fun AttributesMutator.setTypedAttribute(key: String, value: Any) {
    when {
        value is Double -> setDoubleAttribute(key, value)
        value is List<*> && value.isNotEmpty() && value.all { it is Double } ->
            setDoubleListAttribute(key, value.filterIsInstance<Double>())
        else -> setAttributes(mapOf(key to value))
    }
}
