// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PeriodicMetricReader(
  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports. 
   * Value must be non-negative.
   * If omitted or null, 60000 is used.
   */
  public val interval: Long? = null,
  /**
   * Configure maximum allowed time (in milliseconds) to export data. 
   * Value must be non-negative. A value of 0 indicates no limit (infinity).
   * If omitted or null, 30000 is used.
   */
  public val timeout: Long? = null,
  /**
   * Configure maximum export batch size.
   * If omitted or null, no limit is used.
   */
  @SerialName("max_export_batch_size/development")
  public val maxExportBatchSizeDevelopment: Long? = null,
  /**
   * Configure exporter.
   * Property is required and must be non-null.
   */
  public val exporter: PushMetricExporter,
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
