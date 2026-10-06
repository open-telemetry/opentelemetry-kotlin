package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaLoggerBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.factory.ContextFactory

internal class OtelJavaLoggerProviderAdapter(
    private val loggerProvider: LoggerProvider,
    private val contextFactory: ContextFactory,
) : OtelJavaLoggerProvider {

    override fun loggerBuilder(instrumentationScopeName: String): OtelJavaLoggerBuilder = OtelJavaLoggerBuilderAdapter(
        loggerProvider,
        instrumentationScopeName,
        contextFactory,
    )
}
