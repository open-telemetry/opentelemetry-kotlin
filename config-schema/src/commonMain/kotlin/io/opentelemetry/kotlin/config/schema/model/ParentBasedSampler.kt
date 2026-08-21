// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ParentBasedSampler(
  /**
   * Configure root sampler.
   * If omitted, always_on is used.
   */
  public val root: Sampler? = null,
  /**
   * Configure remote_parent_sampled sampler.
   * If omitted, always_on is used.
   */
  @SerialName("remote_parent_sampled")
  public val remoteParentSampled: Sampler? = null,
  /**
   * Configure remote_parent_not_sampled sampler.
   * If omitted, always_off is used.
   */
  @SerialName("remote_parent_not_sampled")
  public val remoteParentNotSampled: Sampler? = null,
  /**
   * Configure local_parent_sampled sampler.
   * If omitted, always_on is used.
   */
  @SerialName("local_parent_sampled")
  public val localParentSampled: Sampler? = null,
  /**
   * Configure local_parent_not_sampled sampler.
   * If omitted, always_off is used.
   */
  @SerialName("local_parent_not_sampled")
  public val localParentNotSampled: Sampler? = null,
)
