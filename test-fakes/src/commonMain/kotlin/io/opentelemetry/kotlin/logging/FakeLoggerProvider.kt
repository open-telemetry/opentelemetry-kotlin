package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AttributesMutator

class FakeLoggerProvider : LoggerProvider {

    val map = mutableMapOf<String, FakeLogger>()

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Logger = map.getOrPut(name) {
        FakeLogger(name)
    }
}
