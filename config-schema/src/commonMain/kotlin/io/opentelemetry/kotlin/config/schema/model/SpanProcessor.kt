// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class SpanProcessor(
  /**
   * Configure a batch span processor.
   * If omitted, ignore.
   */
  internal val batch: BatchSpanProcessor? = null,
  /**
   * Configure a simple span processor.
   * If omitted, ignore.
   */
  internal val simple: SimpleSpanProcessor? = null,
)
