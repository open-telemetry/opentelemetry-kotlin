package io.opentelemetry.kotlin.attributes

/**
 * A canonical immutable [AttributeContainer] that holds no attributes.
 *
 * SDK components that never contribute attributes should return this rather than allocating an
 * empty container per call.
 */
public object EmptyAttributeContainer : AttributeContainer {
    override val attributes: Map<String, Any> = emptyMap()
}
