package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerBuilder

@OptIn(ExperimentalApi::class)
internal class OtelJavaLoggerBuilderAdapter(
    private val loggerProvider: LoggerProvider,
    private val instrumentationScopeName: String
) : OtelJavaLoggerBuilder {

    private var schemaUrl: String? = null
    private var instrumentationScopeVersion: String? = null

    override fun setSchemaUrl(schemaUrl: String): OtelJavaLoggerBuilder {
        this.schemaUrl = schemaUrl
        return this
    }

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): OtelJavaLoggerBuilder {
        this.instrumentationScopeVersion = instrumentationScopeVersion
        return this
    }

    override fun build(): OtelJavaLogger {
        val impl = loggerProvider.getLogger(
            instrumentationScopeName,
            instrumentationScopeVersion,
            schemaUrl
        )
        return OtelJavaLoggerAdapter(impl)
    }
}
