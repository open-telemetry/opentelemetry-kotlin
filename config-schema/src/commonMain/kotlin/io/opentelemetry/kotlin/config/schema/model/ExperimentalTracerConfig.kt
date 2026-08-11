// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalTracerConfig(
  /**
   * Configure if the tracer is enabled or not.
   * If omitted, true is used.
   */
  public val enabled: Boolean? = null,
)
