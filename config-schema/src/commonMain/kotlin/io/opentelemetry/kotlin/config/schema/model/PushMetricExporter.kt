// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PushMetricExporter(
  /**
   * Configure exporter to be OTLP with HTTP transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_http")
  public val otlpHttp: OtlpHttpMetricExporter? = null,
  /**
   * Configure exporter to be OTLP with gRPC transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_grpc")
  public val otlpGrpc: OtlpGrpcMetricExporter? = null,
  /**
   * Configure exporter to be OTLP with file transport.
   * If omitted, ignore.
   */
  @SerialName("otlp_file/development")
  public val otlpFileDevelopment: ExperimentalOtlpFileMetricExporter? = null,
  /**
   * Configure exporter to be console.
   * If omitted, ignore.
   */
  public val console: ConsoleMetricExporter? = null,
)
