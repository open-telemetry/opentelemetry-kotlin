package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.factory.ResourceFactory

class FakeResourceDetector(
    override val name: String = "fake",
    private val attributes: Map<String, String> = emptyMap(),
    private val schemaUrl: String? = null,
    private val error: Throwable? = null,
) : ResourceDetector {

    var detectCount: Int = 0
        private set

    override fun ResourceFactory.detect(): Resource {
        detectCount++
        error?.let { throw it }

        if (attributes.isEmpty() && schemaUrl == null) {
            return empty
        }
        return create(schemaUrl) {
            attributes.forEach { (key, value) -> setStringAttribute(key, value) }
        }
    }
}
