// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class InstrumentType {
  @SerialName("counter")
  COUNTER,
  @SerialName("gauge")
  GAUGE,
  @SerialName("histogram")
  HISTOGRAM,
  @SerialName("observable_counter")
  OBSERVABLE_COUNTER,
  @SerialName("observable_gauge")
  OBSERVABLE_GAUGE,
  @SerialName("observable_up_down_counter")
  OBSERVABLE_UP_DOWN_COUNTER,
  @SerialName("up_down_counter")
  UP_DOWN_COUNTER,
}
