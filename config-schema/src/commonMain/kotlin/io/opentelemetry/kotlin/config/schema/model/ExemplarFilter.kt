// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ExemplarFilter {
  @SerialName("always_on")
  ALWAYS_ON,
  @SerialName("always_off")
  ALWAYS_OFF,
  @SerialName("trace_based")
  TRACE_BASED,
}
