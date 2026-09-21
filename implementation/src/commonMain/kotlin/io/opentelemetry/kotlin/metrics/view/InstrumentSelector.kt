package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor

/**
 * Evaluates the additive instrument and instrumentation-scope criteria of a View.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#instrument-selection-criteria
 */
internal fun interface InstrumentSelector {
    fun matches(
        descriptor: InstrumentDescriptor,
        instrumentationScopeInfo: InstrumentationScopeInfo,
    ): Boolean
}
