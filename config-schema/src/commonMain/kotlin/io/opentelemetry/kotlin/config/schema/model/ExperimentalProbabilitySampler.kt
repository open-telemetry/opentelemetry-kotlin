// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Double
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalProbabilitySampler(
  /**
   * Configure ratio.
   * If omitted or null, 1.0 is used.
   */
  internal val ratio: Double? = null,
)
