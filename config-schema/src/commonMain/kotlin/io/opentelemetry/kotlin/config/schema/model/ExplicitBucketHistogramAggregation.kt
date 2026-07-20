// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.Double
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExplicitBucketHistogramAggregation(
  /**
   * Configure bucket boundaries.
   * If omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000] is used.
   */
  internal val boundaries: List<Double>? = null,
  /**
   * Configure record min and max.
   * If omitted or null, true is used.
   */
  @SerialName("record_min_max")
  internal val recordMinMax: Boolean? = null,
)
