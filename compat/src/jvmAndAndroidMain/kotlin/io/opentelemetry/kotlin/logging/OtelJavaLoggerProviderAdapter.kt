package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider

@OptIn(ExperimentalApi::class)
internal class OtelJavaLoggerProviderAdapter(
    private val loggerProvider: LoggerProvider
) : OtelJavaLoggerProvider {

    override fun loggerBuilder(instrumentationScopeName: String): OtelJavaLoggerBuilder = OtelJavaLoggerBuilderAdapter(
        loggerProvider,
        instrumentationScopeName
    )
}
