// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OtlpHttpMetricExporter(
  /**
   * Configure endpoint.
   * If omitted or null, http://localhost:4318/v1/metrics is used.
   */
  internal val endpoint: String? = null,
  /**
   * Configure TLS settings for the exporter.
   * If omitted, system default TLS settings are used.
   */
  internal val tls: HttpTls? = null,
  /**
   * Configure headers. Entries have higher priority than entries from .headers_list.
   * If an entry's .value is null, the entry is ignored.
   * If omitted, no headers are added.
   */
  internal val headers: List<NameStringValuePair>? = null,
  /**
   * Configure headers. Entries have lower priority than entries from .headers.
   * The value is a list of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options for details.
   * If omitted or null, no headers are added.
   */
  @SerialName("headers_list")
  internal val headersList: String? = null,
  /**
   * Configure compression.
   * Known values include: gzip, none. Implementations may support other compression algorithms.
   * If omitted or null, none is used.
   */
  internal val compression: String? = null,
  /**
   * Configure max time (in milliseconds) to wait for each export.
   * Value must be non-negative. A value of 0 indicates no limit (infinity).
   * If omitted or null, 10000 is used.
   */
  internal val timeout: Long? = null,
  /**
   * Configure the encoding used for messages. 
   * Implementations may not support json.
   * Values include:
   * * json: Protobuf JSON encoding.
   * * protobuf: Protobuf binary encoding.
   * If omitted, protobuf is used.
   */
  internal val encoding: OtlpHttpEncoding? = null,
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
