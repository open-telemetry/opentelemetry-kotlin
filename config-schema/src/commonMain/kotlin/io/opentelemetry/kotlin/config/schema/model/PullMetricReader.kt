// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PullMetricReader(
  /**
   * Configure exporter.
   * Property is required and must be non-null.
   */
  public val exporter: PullMetricExporter,
  /**
   * Configure metric producers.
   * If omitted, no metric producers are added.
   */
  public val producers: List<MetricProducer>? = null,
  /**
   * Configure cardinality limits.
   * If omitted, default values as described in CardinalityLimits are used.
   */
  @SerialName("cardinality_limits")
  public val cardinalityLimits: CardinalityLimits? = null,
)
