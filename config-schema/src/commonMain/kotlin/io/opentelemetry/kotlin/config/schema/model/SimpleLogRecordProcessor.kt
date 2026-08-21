// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class SimpleLogRecordProcessor(
  /**
   * Configure exporter.
   * Property is required and must be non-null.
   */
  public val exporter: LogRecordExporter,
)
