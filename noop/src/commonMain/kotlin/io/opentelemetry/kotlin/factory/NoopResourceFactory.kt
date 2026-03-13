package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.resource.NoopResource
import io.opentelemetry.kotlin.resource.Resource

internal object NoopResourceFactory : ResourceFactory {
    override val empty: Resource = NoopResource

    override fun create(schemaUrl: String?, attributes: AttributesMutator.() -> Unit): Resource =
        NoopResource
}
