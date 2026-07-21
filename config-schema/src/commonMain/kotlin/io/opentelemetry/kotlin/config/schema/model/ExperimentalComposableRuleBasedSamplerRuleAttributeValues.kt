// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalComposableRuleBasedSamplerRuleAttributeValues(
  /**
   * The attribute key to match against.
   * Property is required and must be non-null.
   */
  internal val key: String,
  /**
   * The attribute values to match against. If the attribute's value matches any of these, it matches.
   * Property is required and must be non-null.
   */
  internal val values: List<String>,
)
