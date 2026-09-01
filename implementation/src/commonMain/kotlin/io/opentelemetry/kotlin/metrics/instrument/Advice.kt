package io.opentelemetry.kotlin.metrics.instrument

/**
 * Optional recommendations supplied by instrumentation to influence metric stream configuration.
 *
 * Advice is not part of instrument identity, implementations may ignore it, and an explicit View
 * setting takes precedence over advice for the same aspect of a stream.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-advisory-parameters
 */
internal sealed interface Advice {

    /** Recommended allow-list of attribute keys, or `null` when no recommendation was supplied. */
    val attributeKeys: Set<String>?

    data object Empty : Advice {
        override val attributeKeys: Set<String>? = null
    }

    /** Recommended attribute keys to retain when no matching View configures an allow-list. */
    data class Attributes(
        override val attributeKeys: Set<String>
    ) : Advice

    data class Histogram(
        override val attributeKeys: Set<String>?,

        /**
         * Recommended explicit bucket boundaries used when no View matches or a matching View
         * selects default aggregation. A View selecting explicit-bucket histogram aggregation
         * overrides this advice even when it does not configure boundaries.
         */
        val explicitBucketBoundaries: List<Double>?,
    ) : Advice
}
