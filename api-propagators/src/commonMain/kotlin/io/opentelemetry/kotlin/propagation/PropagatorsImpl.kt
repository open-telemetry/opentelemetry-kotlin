package io.opentelemetry.kotlin.propagation

internal class PropagatorsImpl : Propagators {

    override fun none(): TextMapPropagator = createNoopPropagator()
}
