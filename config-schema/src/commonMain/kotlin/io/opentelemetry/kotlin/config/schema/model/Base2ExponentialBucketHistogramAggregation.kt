// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Base2ExponentialBucketHistogramAggregation(
  /**
   * Configure the max scale factor.
   * If omitted or null, 20 is used.
   */
  @SerialName("max_scale")
  internal val maxScale: Long? = null,
  /**
   * Configure the maximum number of buckets in each of the positive and negative ranges, not counting the special zero bucket.
   * If omitted or null, 160 is used.
   */
  @SerialName("max_size")
  internal val maxSize: Long? = null,
  /**
   * Configure whether or not to record min and max.
   * If omitted or null, true is used.
   */
  @SerialName("record_min_max")
  internal val recordMinMax: Boolean? = null,
)
