package io.opentelemetry.kotlin.resource

class FakeResource(
    override val attributes: Map<String, Any> = mapOf("foo" to "bar"),
    override val schemaUrl: String? = "schemaUrl"
) : Resource {

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        throw UnsupportedOperationException()
    }

    override fun merge(other: Resource): Resource = FakeResource(
        attributes = attributes + other.attributes,
        schemaUrl = other.schemaUrl ?: schemaUrl
    )
}
