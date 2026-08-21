// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalComposableParentThresholdSampler(
  /**
   * Sampler to use when there is no parent.
   * Property is required and must be non-null.
   */
  public val root: ExperimentalComposableSampler,
)
