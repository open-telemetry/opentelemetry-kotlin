// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalLoggerMatcherAndConfig(
  /**
   * Configure logger names to match. Matching is case-sensitive, evaluated as follows:
   *
   *  * If the logger name exactly matches.
   *  * If the logger name matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.
   * Property is required and must be non-null.
   */
  internal val name: String,
  /**
   * The logger config.
   * Property is required and must be non-null.
   */
  internal val config: ExperimentalLoggerConfig,
)
