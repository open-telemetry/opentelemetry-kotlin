package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior

/**
 * Captures the OTLP HTTP logs exporter configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class OtlpHttpLogsExporterConfigDslImpl : BehaviorSupplier<OtlpHttpLogsExporterBehavior> {
    var endpoint: String? = null
    var timeout: Long? = null
    var headers: Map<String, String?>? = null

    override fun toBehavior(): OtlpHttpLogsExporterBehavior {
        return OtlpHttpLogsExporterBehavior(endpoint, timeout, headers)
    }
}
