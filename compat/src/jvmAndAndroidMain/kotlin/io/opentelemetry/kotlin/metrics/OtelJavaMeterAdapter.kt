package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider

internal class OtelJavaMeterAdapter(
    private val impl: Meter,
    private val javaMeterProvider: OtelJavaMeterProvider = OtelJavaMeterProvider.noop(),
) : OtelJavaMeter by javaMeterProvider.get("") {
    override fun upDownCounterBuilder(name: String): OtelJavaLongUpDownCounterBuilder =
        OtelJavaLongUpDownCounterBuilderAdapter(impl, name, javaMeterProvider)
}
