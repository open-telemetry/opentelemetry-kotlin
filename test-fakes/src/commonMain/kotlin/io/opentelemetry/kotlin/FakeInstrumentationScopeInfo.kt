package io.opentelemetry.kotlin
class FakeInstrumentationScopeInfo(
    override val name: String = "name",
    override val version: String? = "version",
    override val schemaUrl: String? = "schemaUrl",
    override val attributes: Map<String, Any> = mapOf("key" to "value"),
    override val droppedAttributesCount: Int = 0,
) : InstrumentationScopeInfo
