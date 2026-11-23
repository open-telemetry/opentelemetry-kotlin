package io.opentelemetry.kotlin.attributes

/**
 * Copies every attribute in [from] onto this mutator, preserving each value's type.
 */
internal fun AttributesMutator.copyAttributes(from: AttributeContainer) {
    copyAttributes(from.typedAttributesOrConvert())
}

/**
 * Applies each typed value in [from] onto this mutator.
 */
internal fun AttributesMutator.copyAttributes(from: Map<String, AttributeValue>) {
    from.forEach { (key, value) -> value.applyTo(key, this) }
}
