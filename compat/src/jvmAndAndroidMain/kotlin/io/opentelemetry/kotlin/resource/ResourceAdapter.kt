package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaResourceBuilder
import io.opentelemetry.kotlin.attributes.convertToMap

@OptIn(ExperimentalApi::class)
internal class ResourceAdapter(
    impl: OtelJavaResource
) : Resource {
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
    override val schemaUrl: String? = impl.schemaUrl

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.toMutableMap(), schemaUrl)
        impl.apply(action)

        val builder = OtelJavaResourceBuilder()
        impl.schemaUrl?.let(builder::setSchemaUrl)

        impl.attributes.forEach {
            builder.put(it.key, it.value.toString())
        }
        return ResourceAdapter(builder.build())
    }
}
