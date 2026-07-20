// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalMeterConfig(
  /**
   * Configure if the meter is enabled or not.
   * If omitted, true is used.
   */
  internal val enabled: Boolean? = null,
)
