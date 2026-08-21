// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class IdGenerator(
  /**
   * Configure the ID generator to randomly generate TraceIds and SpanIds (spec default).
   * If omitted, ignore.
   */
  public val random: RandomIdGenerator? = null,
)
