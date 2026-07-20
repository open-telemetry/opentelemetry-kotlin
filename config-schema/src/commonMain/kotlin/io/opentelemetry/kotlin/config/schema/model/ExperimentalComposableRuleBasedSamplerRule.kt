// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A rule for ExperimentalComposableRuleBasedSampler. A rule can have multiple match conditions - the sampler will be applied if all match. 
 * If no conditions are specified, the rule matches all spans that reach it.
 */
@Serializable
internal data class ExperimentalComposableRuleBasedSamplerRule(
  /**
   * Values to match against a single attribute. Non-string attributes are matched using their string representation:
   * for example, a value of "404" would match the http.response.status_code 404. For array attributes, if any
   * item matches, it is considered a match.
   * If omitted, ignore.
   */
  @SerialName("attribute_values")
  internal val attributeValues: ExperimentalComposableRuleBasedSamplerRuleAttributeValues? = null,
  /**
   * Patterns to match against a single attribute. Non-string attributes are matched using their string representation:
   * for example, a pattern of "4*" would match any http.response.status_code in 400-499. For array attributes, if any
   * item matches, it is considered a match.
   * If omitted, ignore.
   */
  @SerialName("attribute_patterns")
  internal val attributePatterns:
      ExperimentalComposableRuleBasedSamplerRuleAttributePatterns? = null,
  /**
   * The span kinds to match. If the span's kind matches any of these, it matches.
   * Values include:
   * * client: client, a client span.
   * * consumer: consumer, a consumer span.
   * * internal: internal, an internal span.
   * * producer: producer, a producer span.
   * * server: server, a server span.
   * If omitted, ignore.
   */
  @SerialName("span_kinds")
  internal val spanKinds: List<SpanKind>? = null,
  /**
   * The parent span types to match.
   * Values include:
   * * local: local, a local parent.
   * * none: none, no parent, i.e., the trace root.
   * * remote: remote, a remote parent.
   * If omitted, ignore.
   */
  internal val parent: List<ExperimentalSpanParent>? = null,
  /**
   * The sampler to use for matching spans.
   * Property is required and must be non-null.
   */
  internal val sampler: ExperimentalComposableSampler,
)
