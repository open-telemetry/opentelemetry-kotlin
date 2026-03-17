package io.opentelemetry.kotlin.resource

internal object NoopResource : Resource {
    override val attributes: Map<String, Any> = emptyMap()
    override val schemaUrl: String? = null
    override fun asNewResource(action: MutableResource.() -> Unit): Resource = this
    override fun merge(other: Resource): Resource = this
}
