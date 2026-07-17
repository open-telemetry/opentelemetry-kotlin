// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class SimpleSpanProcessor(
  /**
   * Configure exporter.
   * Property is required and must be non-null.
   */
  internal val exporter: SpanExporter,
)
