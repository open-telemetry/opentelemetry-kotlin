package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopPropagatorFactory : PropagatorFactory {

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator =
        NoopTextMapPropagator

    override fun composite(propagators: List<TextMapPropagator>): TextMapPropagator =
        NoopTextMapPropagator

    override fun w3cBaggage(): TextMapPropagator = NoopTextMapPropagator

    override fun w3cTraceContext(): TextMapPropagator = NoopTextMapPropagator
}
