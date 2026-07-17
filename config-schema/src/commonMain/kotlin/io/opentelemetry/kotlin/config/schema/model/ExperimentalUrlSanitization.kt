// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalUrlSanitization(
  /**
   * List of query parameter names whose values should be redacted from URLs.
   * Query parameter names are case-sensitive.
   * This is a full override of the default sensitive query parameter keys, it is not a list of keys in addition to the defaults.
   * Set to an empty array to disable query parameter redaction.
   * If omitted, the default sensitive query parameter list as defined by the url semantic conventions (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md) is used.
   */
  @SerialName("sensitive_query_parameters")
  internal val sensitiveQueryParameters: List<String>? = null,
)
