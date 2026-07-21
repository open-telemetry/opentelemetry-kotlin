// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ViewStream(
  /**
   * Configure metric name of the resulting stream(s).
   * If omitted or null, the instrument's original name is used.
   */
  internal val name: String? = null,
  /**
   * Configure metric description of the resulting stream(s).
   * If omitted or null, the instrument's origin description is used.
   */
  internal val description: String? = null,
  /**
   * Configure aggregation of the resulting stream(s).
   * If omitted, default is used.
   */
  internal val aggregation: Aggregation? = null,
  /**
   * Configure the aggregation cardinality limit.
   * If omitted or null, the metric reader's default cardinality limit is used.
   */
  @SerialName("aggregation_cardinality_limit")
  internal val aggregationCardinalityLimit: Long? = null,
  /**
   * Configure attribute keys retained in the resulting stream(s).
   * If omitted, all attribute keys are retained.
   */
  @SerialName("attribute_keys")
  internal val attributeKeys: IncludeExclude? = null,
)
