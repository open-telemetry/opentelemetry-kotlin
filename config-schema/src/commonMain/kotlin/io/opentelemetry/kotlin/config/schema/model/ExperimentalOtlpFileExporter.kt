// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalOtlpFileExporter(
  /**
   * Configure output stream. 
   * Values include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.
   * If omitted or null, stdout is used.
   */
  @SerialName("output_stream")
  internal val outputStream: String? = null,
)
