// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
internal data class NameStringValuePair(
  /**
   * The name of the pair.
   * Property is required and must be non-null.
   */
  internal val name: String,
  /**
   * The value of the pair.
   * Property must be present, but if null the behavior is dependent on usage context.
   */
  internal val `value`: String? = null,
)
