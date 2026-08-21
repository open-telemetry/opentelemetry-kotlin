// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalComposableRuleBasedSampler(
  /**
   * The rules for the sampler, matched in order.
   * Each rule can have multiple match conditions. All conditions must match for the rule to match.
   * If no conditions are specified, the rule matches all spans that reach it.
   * If no rules match, the span is not sampled.
   * If omitted, no span is sampled.
   */
  public val rules: List<ExperimentalComposableRuleBasedSamplerRule>? = null,
)
