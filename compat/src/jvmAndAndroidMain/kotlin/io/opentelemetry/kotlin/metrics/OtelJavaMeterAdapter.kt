package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider

/**
 * Adapts a Kotlin [Meter] to a Java [OtelJavaMeter].
 *
 * UpDownCounter builders are wired to the Kotlin API. Other instrument-builder calls still
 * delegate to Java OTel's no-op meter until those instruments are implemented.
 */
internal class OtelJavaMeterAdapter(
    private val impl: Meter,
) : OtelJavaMeter by OtelJavaMeterProvider.noop().get("") {
    override fun upDownCounterBuilder(name: String): OtelJavaLongUpDownCounterBuilder =
        OtelJavaLongUpDownCounterBuilderAdapter(impl, name)
}
