// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Sampler(
  /**
   * Configure sampler to be always_off.
   * If omitted, ignore.
   */
  @SerialName("always_off")
  public val alwaysOff: AlwaysOffSampler? = null,
  /**
   * Configure sampler to be always_on.
   * If omitted, ignore.
   */
  @SerialName("always_on")
  public val alwaysOn: AlwaysOnSampler? = null,
  /**
   * Configure sampler to be composite.
   * If omitted, ignore.
   */
  @SerialName("composite/development")
  public val compositeDevelopment: ExperimentalComposableSampler? = null,
  /**
   * Configure sampler to be jaeger_remote.
   * If omitted, ignore.
   */
  @SerialName("jaeger_remote/development")
  public val jaegerRemoteDevelopment: ExperimentalJaegerRemoteSampler? = null,
  /**
   * Configure sampler to be parent_based.
   * If omitted, ignore.
   */
  @SerialName("parent_based")
  public val parentBased: ParentBasedSampler? = null,
  /**
   * Configure sampler to be probability.
   * If omitted, ignore.
   */
  @SerialName("probability/development")
  public val probabilityDevelopment: ExperimentalProbabilitySampler? = null,
  /**
   * Configure sampler to be trace_id_ratio_based.
   * If omitted, ignore.
   */
  @SerialName("trace_id_ratio_based")
  public val traceIdRatioBased: TraceIdRatioBasedSampler? = null,
)
