// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalOtlpFileMetricExporter(
  /**
   * Configure output stream. 
   * Values include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.
   * If omitted or null, stdout is used.
   */
  @SerialName("output_stream")
  internal val outputStream: String? = null,
  /**
   * Configure temporality preference.
   * Values include:
   * * cumulative: Use cumulative aggregation temporality for all instrument types.
   * * delta: Use delta aggregation for all instrument types except up down counter and asynchronous up down counter.
   * * low_memory: Use delta aggregation temporality for counter and histogram instrument types. Use cumulative aggregation temporality for all other instrument types.
   * If omitted, cumulative is used.
   */
  @SerialName("temporality_preference")
  internal val temporalityPreference: ExporterTemporalityPreference? = null,
  /**
   * Configure default histogram aggregation.
   * Values include:
   * * base2_exponential_bucket_histogram: Use base2 exponential histogram as the default aggregation for histogram instruments.
   * * explicit_bucket_histogram: Use explicit bucket histogram as the default aggregation for histogram instruments.
   * If omitted, explicit_bucket_histogram is used.
   */
  @SerialName("default_histogram_aggregation")
  internal val defaultHistogramAggregation: ExporterDefaultHistogramAggregation? = null,
)
