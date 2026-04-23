package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaResourceBuilder
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter

internal object CompatResourceFactory : ResourceFactory {
    override val empty: Resource = ResourceAdapter(OtelJavaResourceBuilder().build())

    override fun create(schemaUrl: String?, attributes: AttributesMutator.() -> Unit): Resource {
        val attrs: CompatAttributesModel = CompatAttributesModel().apply { attributes() }
        return ResourceAdapter(
            OtelJavaResourceBuilder().apply {
                schemaUrl?.let { setSchemaUrl(it) }
                putAll(attrs.attrs.build())
            }.build()
        )
    }
}
