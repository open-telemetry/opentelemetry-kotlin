// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalHttpClientInstrumentation(
  /**
   * Configure headers to capture for outbound http requests.
   * If omitted, no outbound request headers are captured.
   */
  @SerialName("request_captured_headers")
  internal val requestCapturedHeaders: List<String>? = null,
  /**
   * Configure headers to capture for inbound http responses.
   * If omitted, no inbound response headers are captured.
   */
  @SerialName("response_captured_headers")
  internal val responseCapturedHeaders: List<String>? = null,
  /**
   * Override the default list of known HTTP methods.
   * Known methods are case-sensitive.
   * This is a full override of the default known methods, not a list of known methods in addition to the defaults.
   * If omitted, HTTP methods GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH are known.
   */
  @SerialName("known_methods")
  internal val knownMethods: List<String>? = null,
)
