package io.opentelemetry.kotlin

@OptIn(ExperimentalApi::class)
class FakeInstrumentationScopeInfo(
    override val name: String = "name",
    override val version: String? = "version",
    override val schemaUrl: String? = "schemaUrl",
    override val attributes: Map<String, Any> = mapOf("key" to "value")
) : InstrumentationScopeInfo
