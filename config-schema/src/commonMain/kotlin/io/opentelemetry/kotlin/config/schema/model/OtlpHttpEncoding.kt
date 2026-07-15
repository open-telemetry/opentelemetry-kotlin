// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class OtlpHttpEncoding {
  @SerialName("protobuf")
  PROTOBUF,
  @SerialName("json")
  JSON,
}
