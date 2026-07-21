// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ParentBasedSampler(
  /**
   * Configure root sampler.
   * If omitted, always_on is used.
   */
  internal val root: Sampler? = null,
  /**
   * Configure remote_parent_sampled sampler.
   * If omitted, always_on is used.
   */
  @SerialName("remote_parent_sampled")
  internal val remoteParentSampled: Sampler? = null,
  /**
   * Configure remote_parent_not_sampled sampler.
   * If omitted, always_off is used.
   */
  @SerialName("remote_parent_not_sampled")
  internal val remoteParentNotSampled: Sampler? = null,
  /**
   * Configure local_parent_sampled sampler.
   * If omitted, always_on is used.
   */
  @SerialName("local_parent_sampled")
  internal val localParentSampled: Sampler? = null,
  /**
   * Configure local_parent_not_sampled sampler.
   * If omitted, always_off is used.
   */
  @SerialName("local_parent_not_sampled")
  internal val localParentNotSampled: Sampler? = null,
)
