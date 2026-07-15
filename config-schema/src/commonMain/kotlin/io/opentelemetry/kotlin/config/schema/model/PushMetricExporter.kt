// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PushMetricExporter(
  /**
   * Configure exporter to be OTLP with HTTP transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_http")
  internal val otlpHttp: OtlpHttpMetricExporter? = null,
  /**
   * Configure exporter to be OTLP with gRPC transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_grpc")
  internal val otlpGrpc: OtlpGrpcMetricExporter? = null,
  /**
   * Configure exporter to be OTLP with file transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_file/development")
  internal val otlpFileDevelopment: ExperimentalOtlpFileMetricExporter? = null,
  /**
   * Configure exporter to be console.
   * If omitted, ignore.
   */
  internal val console: ConsoleMetricExporter? = null,
)
