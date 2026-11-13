package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterBuilder
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class OtelJavaMeterBuilderAdapter(
    private val meterProvider: MeterProvider,
    private val instrumentationScopeName: String,
    private val sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
) : OtelJavaMeterBuilder {

    private var schemaUrl: String? = null
    private var instrumentationScopeVersion: String? = null

    override fun setSchemaUrl(schemaUrl: String): OtelJavaMeterBuilder {
        this.schemaUrl = schemaUrl
        return this
    }

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): OtelJavaMeterBuilder {
        this.instrumentationScopeVersion = instrumentationScopeVersion
        return this
    }

    override fun build(): OtelJavaMeter {
        val impl = meterProvider.getMeter(
            instrumentationScopeName,
            instrumentationScopeVersion,
            schemaUrl
        )
        return OtelJavaMeterAdapter(impl, sdkErrorHandler = sdkErrorHandler)
    }
}
