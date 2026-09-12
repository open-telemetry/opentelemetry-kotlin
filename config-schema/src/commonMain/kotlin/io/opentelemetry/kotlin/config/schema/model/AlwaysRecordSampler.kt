// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class AlwaysRecordSampler(
  /**
   * Configure the wrapped sampler which provides the original sampling
   * decision that AlwaysRecord modifies. DROP decisions are converted
   * to RECORD_ONLY, allowing processors to see all spans without sending them to exporters.
   * Property is required and must be non-null.
   */
  public val root: Sampler,
)
