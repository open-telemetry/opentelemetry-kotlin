// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ExporterTemporalityPreference {
  @SerialName("cumulative")
  CUMULATIVE,
  @SerialName("delta")
  DELTA,
  @SerialName("low_memory")
  LOW_MEMORY,
}
