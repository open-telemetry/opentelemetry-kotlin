package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap

@ExperimentalApi
internal class LoggerProviderAdapter(
    private val impl: OtelJavaLoggerProvider,
) : LoggerProvider, TelemetryCloseable {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, LoggerAdapter>()

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Logger {
        return map.getOrPut(scopeCacheKey(name, version, schemaUrl)) {
            val builder = impl.loggerBuilder(name)

            if (schemaUrl != null) {
                builder.setSchemaUrl(schemaUrl)
            }
            if (version != null) {
                builder.setInstrumentationVersion(version)
            }
            LoggerAdapter(builder.build())
        }
    }

    override suspend fun forceFlush(): OperationResultCode = when (impl) {
        is OtelJavaSdkLoggerProvider -> awaitOperationResultCode { impl.forceFlush() }
        else -> OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = when (impl) {
        is OtelJavaSdkLoggerProvider -> awaitOperationResultCode { impl.shutdown() }
        else -> OperationResultCode.Success
    }
}
