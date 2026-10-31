package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.factory.ResourceFactory

class FakeResourceDetector(
    name: String = "fake",
    private val attributes: Map<String, String> = emptyMap(),
    private val schemaUrl: String? = null,
    private val error: Throwable? = null,
    private val nameError: Throwable? = null,
) : ResourceDetector {

    private val detectorName = name

    override val name: String
        get() {
            nameError?.let { throw it }
            return detectorName
        }

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
