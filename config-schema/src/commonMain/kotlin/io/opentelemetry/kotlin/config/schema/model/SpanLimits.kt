// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SpanLimits(
  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit. 
   * Value must be non-negative.
   * If omitted or null, there is no limit.
   */
  @SerialName("attribute_value_length_limit")
  internal val attributeValueLengthLimit: Long? = null,
  /**
   * Configure max attribute count. Overrides .attribute_limits.attribute_count_limit. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("attribute_count_limit")
  internal val attributeCountLimit: Long? = null,
  /**
   * Configure max span event count. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("event_count_limit")
  internal val eventCountLimit: Long? = null,
  /**
   * Configure max span link count. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("link_count_limit")
  internal val linkCountLimit: Long? = null,
  /**
   * Configure max attributes per span event. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("event_attribute_count_limit")
  internal val eventAttributeCountLimit: Long? = null,
  /**
   * Configure max attributes per span link. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("link_attribute_count_limit")
  internal val linkAttributeCountLimit: Long? = null,
)
