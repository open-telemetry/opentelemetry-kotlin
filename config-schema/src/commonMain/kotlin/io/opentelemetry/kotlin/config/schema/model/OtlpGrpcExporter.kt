// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OtlpGrpcExporter(
  /**
   * Configure endpoint.
   * If omitted or null, http://localhost:4317 is used.
   */
  internal val endpoint: String? = null,
  /**
   * Configure TLS settings for the exporter.
   * If omitted, system default TLS settings are used.
   */
  internal val tls: GrpcTls? = null,
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
)
