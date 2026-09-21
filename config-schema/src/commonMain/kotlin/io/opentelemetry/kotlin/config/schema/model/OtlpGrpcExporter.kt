// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OtlpGrpcExporter(
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
   * Configure the maximum size of each export request message in bytes, before compression.
   * Value must be non-negative. A value of 0 indicates no limit (infinity) and is not recommended.
   * If omitted or null, 67108864 (64 MiB) is used.
   */
  @SerialName("max_request_size")
  public val maxRequestSize: Long? = null,
  /**
   * Configure the maximum size of each export response in bytes, after decompression.
   * Value must be positive.
   * If omitted or null, 4194304 (4 MiB) is used.
   */
  @SerialName("max_response_size")
  public val maxResponseSize: Long? = null,
  /**
   * Configure max time (in milliseconds) to wait for each export.
   * Value must be non-negative. A value of 0 indicates no limit (infinity).
   * If omitted or null, 10000 is used.
   */
  public val timeout: Long? = null,
)
