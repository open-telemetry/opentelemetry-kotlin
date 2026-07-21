// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Sampler(
  /**
   * Configure sampler to be always_off.
   * If omitted, ignore.
   */
  @SerialName("always_off")
  internal val alwaysOff: AlwaysOffSampler? = null,
  /**
   * Configure sampler to be always_on.
   * If omitted, ignore.
   */
  @SerialName("always_on")
  internal val alwaysOn: AlwaysOnSampler? = null,
  /**
   * Configure sampler to be composite.
   * If omitted, ignore.
   */
  @SerialName("composite/development")
  internal val compositeDevelopment: ExperimentalComposableSampler? = null,
  /**
   * Configure sampler to be jaeger_remote.
   * If omitted, ignore.
   */
  @SerialName("jaeger_remote/development")
  internal val jaegerRemoteDevelopment: ExperimentalJaegerRemoteSampler? = null,
  /**
   * Configure sampler to be parent_based.
   * If omitted, ignore.
   */
  @SerialName("parent_based")
  internal val parentBased: ParentBasedSampler? = null,
  /**
   * Configure sampler to be probability.
   * If omitted, ignore.
   */
  @SerialName("probability/development")
  internal val probabilityDevelopment: ExperimentalProbabilitySampler? = null,
  /**
   * Configure sampler to be trace_id_ratio_based.
   * If omitted, ignore.
   */
  @SerialName("trace_id_ratio_based")
  internal val traceIdRatioBased: TraceIdRatioBasedSampler? = null,
)
