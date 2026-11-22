package io.opentelemetry.kotlin.attributes

/**
 * An [AttributeContainer] that also exposes its attributes in typed form, so consumers don't need
 * to infer each value's type from [AttributeContainer.attributes].
 */
public interface TypedAttributeContainer : AttributeContainer {

    /**
     * The same attributes as [attributes], in typed form.
     */
    public val typedAttributes: Map<String, AttributeValue>
}

/**
 * Returns this container's typed attributes, converting each value with [AttributeValue.fromPublic]
 * when the container doesn't implement [TypedAttributeContainer].
 */
public fun AttributeContainer.typedAttributesOrConvert(): Map<String, AttributeValue> = when (this) {
    is TypedAttributeContainer -> typedAttributes
    else -> attributes.toAttributeValues()
}

/**
 * Converts each value in a map read from [AttributeContainer.attributes] with
 * [AttributeValue.fromPublic].
 */
public fun Map<String, Any>.toAttributeValues(): Map<String, AttributeValue> =
    mapValues { AttributeValue.fromPublic(it.value) }
