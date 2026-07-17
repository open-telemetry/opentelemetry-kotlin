// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalSanitization(
  /**
   * Configure URL sanitization options.
   * If omitted, defaults as described in ExperimentalUrlSanitization are used.
   */
  internal val url: ExperimentalUrlSanitization? = null,
)
