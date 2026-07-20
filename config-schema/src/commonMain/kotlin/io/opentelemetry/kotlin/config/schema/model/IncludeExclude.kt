// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
internal data class IncludeExclude(
  /**
   * Configure list of value patterns to include.
   * Matching is case-sensitive. Values are evaluated to match as follows:
   *  * If the value exactly matches.
   *  * If the value matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.
   * If omitted, all values are included.
   */
  internal val included: List<String>? = null,
  /**
   * Configure list of value patterns to exclude. Applies after .included (i.e. excluded has higher priority than included).
   * Matching is case-sensitive. Values are evaluated to match as follows:
   *  * If the value exactly matches.
   *  * If the value matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.
   * If omitted, .included attributes are included.
   */
  internal val excluded: List<String>? = null,
)
