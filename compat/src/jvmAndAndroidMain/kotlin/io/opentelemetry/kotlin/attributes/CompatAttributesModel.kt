package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaAttributesBuilder

internal class CompatAttributesModel(
    internal val attrs: OtelJavaAttributesBuilder = OtelJavaAttributes.builder()
) : AttributesMutator by OtelJavaAttributesMutator(attrs.attributeSink()), AttributeContainer {

    override val attributes: Map<String, Any>
        get() = attrs.build().convertToMap()

    fun otelJavaAttributes(): OtelJavaAttributes = attrs.build()
}
