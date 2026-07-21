// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalComposableSampler(
  /**
   * Configure sampler to be always_off.
   * If omitted, ignore.
   */
  @SerialName("always_off")
  internal val alwaysOff: ExperimentalComposableAlwaysOffSampler? = null,
  /**
   * Configure sampler to be always_on.
   * If omitted, ignore.
   */
  @SerialName("always_on")
  internal val alwaysOn: ExperimentalComposableAlwaysOnSampler? = null,
  /**
   * Configure sampler to be parent_threshold.
   * If omitted, ignore.
   */
  @SerialName("parent_threshold")
  internal val parentThreshold: ExperimentalComposableParentThresholdSampler? = null,
  /**
   * Configure sampler to be probability.
   * If omitted, ignore.
   */
  internal val probability: ExperimentalComposableProbabilitySampler? = null,
  /**
   * Configure sampler to be rule_based.
   * If omitted, ignore.
   */
  @SerialName("rule_based")
  internal val ruleBased: ExperimentalComposableRuleBasedSampler? = null,
)
