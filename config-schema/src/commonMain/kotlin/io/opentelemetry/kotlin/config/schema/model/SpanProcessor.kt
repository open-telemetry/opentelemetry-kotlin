// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class SpanProcessor(
  /**
   * Configure a batch span processor.
   * If omitted, ignore.
   */
  public val batch: BatchSpanProcessor? = null,
  /**
   * Configure a simple span processor.
   * If omitted, ignore.
   */
  public val simple: SimpleSpanProcessor? = null,
)
