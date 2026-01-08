package io.opentelemetry.kotlin

@OptIn(ExperimentalApi::class)
data class InstrumentationScopeInfoImpl(
    override val name: String,
    override val version: String?,
    override val schemaUrl: String?,
    override val attributes: Map<String, Any>
) : InstrumentationScopeInfo
