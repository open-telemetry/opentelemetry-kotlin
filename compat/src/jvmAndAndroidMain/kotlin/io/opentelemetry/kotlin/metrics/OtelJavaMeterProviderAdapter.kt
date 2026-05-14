package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaMeterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider

internal class OtelJavaMeterProviderAdapter(
    private val meterProvider: MeterProvider
) : OtelJavaMeterProvider {

    override fun meterBuilder(instrumentationScopeName: String): OtelJavaMeterBuilder =
        OtelJavaMeterBuilderAdapter(meterProvider, instrumentationScopeName)
}
