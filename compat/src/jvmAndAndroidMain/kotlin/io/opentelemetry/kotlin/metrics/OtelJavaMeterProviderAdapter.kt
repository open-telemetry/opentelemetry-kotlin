package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaMeterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class OtelJavaMeterProviderAdapter(
    private val meterProvider: MeterProvider,
    private val sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
) : OtelJavaMeterProvider {

    override fun meterBuilder(instrumentationScopeName: String): OtelJavaMeterBuilder =
        OtelJavaMeterBuilderAdapter(meterProvider, instrumentationScopeName, sdkErrorHandler)
}
