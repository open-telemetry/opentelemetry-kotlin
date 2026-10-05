package io.opentelemetry.kotlin.attributes

/**
 * Copies attributes that are already stored in an [AttributeContainer] onto an [AttributesMutator],
 * preserving the double-ness of their values.
 *
 * The public [setAttributes] widens any whole-valued number to a long, because Kotlin/JS can't
 * tell [Int] from [Double] at runtime. Values held by an [AttributesModel] have already been
 * typed by a setter so doubles are dispatched here.
 */
internal fun AttributesMutator.copyTypedAttributes(attributes: Map<String, Any>) {
    attributes.forEach { (key, value) ->
        when {
            value is Double -> setDoubleAttribute(key, value)
            value is List<*> && value.isNotEmpty() && value.all { it is Double } ->
                setDoubleListAttribute(key, value.filterIsInstance<Double>())
            else -> setAttributes(mapOf(key to value))
        }
    }
}
