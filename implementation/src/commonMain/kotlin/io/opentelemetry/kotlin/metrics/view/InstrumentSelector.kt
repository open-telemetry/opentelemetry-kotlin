package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind

/**
 * Instrument selection criteria of a View, evaluated by [ViewRegistry].
 * Every criterion is optional and criteria are additive:
 * - an instrument must satisfy all criteria that are set
 * - a selector without criteria matches every instrument
 *
 * [name] supports the wildcards `*`, matching any number of characters including none, and `?`,
 * matching exactly one character. Like instrument names it is compared case-insensitively. Every
 * other criterion must equal the instrument's or meter's value exactly, where an instrument
 * without a unit has the empty unit. [kind] is the criterion the specification calls `type`.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#instrument-selection-criteria
 */
internal data class InstrumentSelector(
    val name: String? = null,
    val kind: InstrumentKind? = null,
    val unit: String? = null,
    val meterName: String? = null,
    val meterVersion: String? = null,
    val meterSchemaUrl: String? = null,
)
