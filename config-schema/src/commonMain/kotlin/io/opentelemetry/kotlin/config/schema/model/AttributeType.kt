// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class AttributeType {
  @SerialName("string")
  STRING,
  @SerialName("bool")
  BOOL,
  @SerialName("int")
  INT,
  @SerialName("double")
  DOUBLE,
  @SerialName("string_array")
  STRING_ARRAY,
  @SerialName("bool_array")
  BOOL_ARRAY,
  @SerialName("int_array")
  INT_ARRAY,
  @SerialName("double_array")
  DOUBLE_ARRAY,
}
