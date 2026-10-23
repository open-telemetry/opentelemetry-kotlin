package io.opentelemetry.kotlin.resource

class FakeEntity(
    override val type: String = "type",
    override val schemaUrl: String? = "schemaUrl",
    override val id: Map<String, Any> = mapOf("foo" to "bar"),
    override val description: Map<String, Any> = emptyMap()
) : Entity
