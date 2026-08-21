// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class NameStringValuePair(
  /**
   * The name of the pair.
   * Property is required and must be non-null.
   */
  public val name: String,
  /**
   * The value of the pair.
   * Property must be present, but if null the behavior is dependent on usage context.
   */
  public val `value`: String? = null,
)
