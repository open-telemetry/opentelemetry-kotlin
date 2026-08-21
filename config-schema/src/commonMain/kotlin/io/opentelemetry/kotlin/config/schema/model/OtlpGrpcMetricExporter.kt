// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OtlpGrpcMetricExporter(
  /**
   * Configure endpoint.
   * If omitted or null, http://localhost:4317 is used.
   */
  public val endpoint: String? = null,
  /**
   * Configure TLS settings for the exporter.
   * If omitted, system default TLS settings are used.
   */
  public val tls: GrpcTls? = null,
  /**
   * Configure headers. Entries have higher priority than entries from .headers_list.
   * If an entry's .value is null, the entry is ignored.
   * If omitted, no headers are added.
   */
  public val headers: List<NameStringValuePair>? = null,
  /**
   * Configure headers. Entries have lower priority than entries from .headers.
   * The value is a list of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options for details.
   * If omitted or null, no headers are added.
   */
  @SerialName("headers_list")
  public val headersList: String? = null,
  /**
   * Configure compression.
   * Known values include: gzip, none. Implementations may support other compression algorithms.
   * If omitted or null, none is used.
   */
  public val compression: String? = null,
  /**
   * Configure max time (in milliseconds) to wait for each export.
   * Value must be non-negative. A value of 0 indicates no limit (infinity).
   * If omitted or null, 10000 is used.
   */
  public val timeout: Long? = null,
  /**
   * Configure temporality preference.
   * Values include:
   * * cumulative: Use cumulative aggregation temporality for all instrument types.
   * * delta: Use delta aggregation for all instrument types except up down counter and asynchronous up down counter.
   * * low_memory: Use delta aggregation temporality for counter and histogram instrument types. Use cumulative aggregation temporality for all other instrument types.
   * If omitted, cumulative is used.
   */
  @SerialName("temporality_preference")
  public val temporalityPreference: ExporterTemporalityPreference? = null,
  /**
   * Configure default histogram aggregation.
   * Values include:
   * * base2_exponential_bucket_histogram: Use base2 exponential histogram as the default aggregation for histogram instruments.
   * * explicit_bucket_histogram: Use explicit bucket histogram as the default aggregation for histogram instruments.
   * If omitted, explicit_bucket_histogram is used.
   */
  @SerialName("default_histogram_aggregation")
  public val defaultHistogramAggregation: ExporterDefaultHistogramAggregation? = null,
)
