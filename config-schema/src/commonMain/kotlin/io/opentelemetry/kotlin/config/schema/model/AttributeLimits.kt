// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AttributeLimits(
  /**
   * Configure max attribute value size. 
   * Value must be non-negative.
   * If omitted or null, there is no limit.
   */
  @SerialName("attribute_value_length_limit")
  public val attributeValueLengthLimit: Long? = null,
  /**
   * Configure max attribute count. 
   * Value must be non-negative.
   * If omitted or null, 128 is used.
   */
  @SerialName("attribute_count_limit")
  public val attributeCountLimit: Long? = null,
)
