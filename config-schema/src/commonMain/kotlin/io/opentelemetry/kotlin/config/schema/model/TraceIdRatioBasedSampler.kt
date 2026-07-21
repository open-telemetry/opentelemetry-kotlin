// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Double
import kotlinx.serialization.Serializable

@Serializable
internal data class TraceIdRatioBasedSampler(
  /**
   * Configure trace_id_ratio.
   * If omitted or null, 1.0 is used.
   */
  internal val ratio: Double? = null,
)
