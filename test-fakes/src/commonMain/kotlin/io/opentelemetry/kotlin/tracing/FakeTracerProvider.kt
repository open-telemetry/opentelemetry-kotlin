package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

@OptIn(ExperimentalApi::class)
class FakeTracerProvider : TracerProvider {

    val map = mutableMapOf<String, FakeTracer>()

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ): Tracer = map.getOrPut(name) {
        FakeTracer(name)
    }
}
