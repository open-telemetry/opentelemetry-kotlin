// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalSanitization(
  /**
   * Configure URL sanitization options.
   * If omitted, defaults as described in ExperimentalUrlSanitization are used.
   */
  public val url: ExperimentalUrlSanitization? = null,
)
