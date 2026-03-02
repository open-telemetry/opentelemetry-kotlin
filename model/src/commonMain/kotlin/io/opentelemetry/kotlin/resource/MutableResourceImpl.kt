package io.opentelemetry.kotlin.resource
class MutableResourceImpl(
    override val attributes: MutableMap<String, Any>,
    override var schemaUrl: String?,
) : MutableResource
