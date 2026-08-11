// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CardinalityLimits(
  /**
   * Configure default cardinality limit for all instrument types.
   * Instrument-specific cardinality limits take priority.
   * If omitted or null, 2000 is used.
   */
  public val default: Long? = null,
  /**
   * Configure default cardinality limit for counter instruments.
   * If omitted or null, the value from .default is used.
   */
  public val counter: Long? = null,
  /**
   * Configure default cardinality limit for gauge instruments.
   * If omitted or null, the value from .default is used.
   */
  public val gauge: Long? = null,
  /**
   * Configure default cardinality limit for histogram instruments.
   * If omitted or null, the value from .default is used.
   */
  public val histogram: Long? = null,
  /**
   * Configure default cardinality limit for observable_counter instruments.
   * If omitted or null, the value from .default is used.
   */
  @SerialName("observable_counter")
  public val observableCounter: Long? = null,
  /**
   * Configure default cardinality limit for observable_gauge instruments.
   * If omitted or null, the value from .default is used.
   */
  @SerialName("observable_gauge")
  public val observableGauge: Long? = null,
  /**
   * Configure default cardinality limit for observable_up_down_counter instruments.
   * If omitted or null, the value from .default is used.
   */
  @SerialName("observable_up_down_counter")
  public val observableUpDownCounter: Long? = null,
  /**
   * Configure default cardinality limit for up_down_counter instruments.
   * If omitted or null, the value from .default is used.
   */
  @SerialName("up_down_counter")
  public val upDownCounter: Long? = null,
)
