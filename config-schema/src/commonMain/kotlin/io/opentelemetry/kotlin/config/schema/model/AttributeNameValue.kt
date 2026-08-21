// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Any
import kotlin.String
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
public data class AttributeNameValue(
  /**
   * The attribute name.
   * Property is required and must be non-null.
   */
  public val name: String,
  /**
   * The attribute value.
   * The type of value must match .type.
   * Property must be present, but if null the entry is ignored.
   */
  public val `value`: @Contextual Any? = null,
  /**
   * The attribute type.
   * Values include:
   * * bool: Boolean attribute value.
   * * bool_array: Boolean array attribute value.
   * * double: Double attribute value.
   * * double_array: Double array attribute value.
   * * int: Integer attribute value.
   * * int_array: Integer array attribute value.
   * * string: String attribute value.
   * * string_array: String array attribute value.
   * If omitted, string is used.
   */
  public val type: AttributeType? = null,
)
