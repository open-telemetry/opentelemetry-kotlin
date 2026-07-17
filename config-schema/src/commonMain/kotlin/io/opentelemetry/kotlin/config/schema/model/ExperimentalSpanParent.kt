// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ExperimentalSpanParent {
  @SerialName("none")
  NONE,
  @SerialName("remote")
  REMOTE,
  @SerialName("local")
  LOCAL,
}
