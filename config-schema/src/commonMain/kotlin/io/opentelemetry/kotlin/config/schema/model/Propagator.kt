// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Propagator(
  /**
   * Configure the propagators in the composite text map propagator. Entries from .composite_list are appended to the list here with duplicates filtered out.
   * Built-in propagator keys include: tracecontext, baggage, b3, b3multi. Known third party keys include: xray.
   * If omitted, and .composite_list is omitted or null, a noop propagator is used.
   */
  internal val composite: List<TextMapPropagator>? = null,
  /**
   * Configure the propagators in the composite text map propagator. Entries are appended to .composite with duplicates filtered out.
   * The value is a comma separated list of propagator identifiers matching the format of OTEL_PROPAGATORS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration for details.
   * Built-in propagator identifiers include: tracecontext, baggage, b3, b3multi. Known third party identifiers include: xray.
   * If omitted or null, and .composite is omitted or null, a noop propagator is used.
   */
  @SerialName("composite_list")
  internal val compositeList: String? = null,
)
