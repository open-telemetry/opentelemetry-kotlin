package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior

/**
 * Captures the OTLP HTTP span exporter configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class OtlpHttpSpanExporterConfigDslImpl : BehaviorSupplier<OtlpHttpSpanExporterBehavior> {
    var endpoint: String? = null
    var timeout: Long? = null
    var headers: Map<String, String?>? = null

    override fun toBehavior(): OtlpHttpSpanExporterBehavior {
        return OtlpHttpSpanExporterBehavior(endpoint, timeout, headers)
    }
}
