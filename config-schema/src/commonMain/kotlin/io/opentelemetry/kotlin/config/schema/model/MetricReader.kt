// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class MetricReader(
  /**
   * Configure a periodic metric reader.
   * If omitted, ignore.
   */
  internal val periodic: PeriodicMetricReader? = null,
  /**
   * Configure a pull based metric reader.
   * If omitted, ignore.
   */
  internal val pull: PullMetricReader? = null,
)
