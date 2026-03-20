package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.SpanFactory

/**
 * Configures how traces are sampled.
 */
@ExperimentalApi
@ConfigDsl
public interface SamplerConfigDsl {

    /**
     * The [SpanFactory] implementation that will be used by the OpenTelemetry implementation.
     */
    public val spanFactory: SpanFactory
}
