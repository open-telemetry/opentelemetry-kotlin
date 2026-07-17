// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Aggregation(
  /**
   * Configures the stream to use the instrument kind to select an aggregation and advisory parameters to influence aggregation configuration parameters. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#default-aggregation for details.
   * If omitted, ignore.
   */
  internal val default: DefaultAggregation? = null,
  /**
   * Configures the stream to ignore/drop all instrument measurements. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#drop-aggregation for details.
   * If omitted, ignore.
   */
  internal val drop: DropAggregation? = null,
  /**
   * Configures the stream to collect data for the histogram metric point using a set of explicit boundary values for histogram bucketing. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#explicit-bucket-histogram-aggregation for details
   * If omitted, ignore.
   */
  @SerialName("explicit_bucket_histogram")
  internal val explicitBucketHistogram: ExplicitBucketHistogramAggregation? = null,
  /**
   * Configures the stream to collect data for the exponential histogram metric point, which uses a base-2 exponential formula to determine bucket boundaries and an integer scale parameter to control resolution. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#base2-exponential-bucket-histogram-aggregation for details.
   * If omitted, ignore.
   */
  @SerialName("base2_exponential_bucket_histogram")
  internal val base2ExponentialBucketHistogram: Base2ExponentialBucketHistogramAggregation? = null,
  /**
   * Configures the stream to collect data using the last measurement. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#last-value-aggregation for details.
   * If omitted, ignore.
   */
  @SerialName("last_value")
  internal val lastValue: LastValueAggregation? = null,
  /**
   * Configures the stream to collect the arithmetic sum of measurement values. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#sum-aggregation for details.
   * If omitted, ignore.
   */
  internal val sum: SumAggregation? = null,
)
