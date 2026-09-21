// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LogRecordLimits(
  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit. 
   * Value must be non-negative.
   * If omitted or null, there is no limit.
   */
  @SerialName("attribute_value_length_limit")
  public val attributeValueLengthLimit: Long? = null,
  /**
   * Configure the maximum attribute value depth for nested array and map values. Overrides .attribute_limits.attribute_value_depth_limit.
   * Depth starts at 1 for the top-level attribute value and increments when descending into array elements or map values.
   * Array or map values deeper than the limit are replaced with an empty value.
   * Value must be positive.
   * If omitted or null, 64 is used.
   */
  @SerialName("attribute_value_depth_limit")
  public val attributeValueDepthLimit: Long? = null,
  /**
   * Configure max attribute count. Overrides .attribute_limits.attribute_count_limit. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("attribute_count_limit")
  public val attributeCountLimit: Long? = null,
)
