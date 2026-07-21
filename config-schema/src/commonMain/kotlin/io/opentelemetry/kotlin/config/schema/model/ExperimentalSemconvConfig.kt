// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalSemconvConfig(
  /**
   * The target semantic convention version for this domain (e.g., 1).
   * If omitted or null, the latest stable version is used, or if no stable version is available and .experimental is true then the latest experimental version is used.
   */
  internal val version: Long? = null,
  /**
   * Use latest experimental semantic conventions (before stable is available or to enable experimental features on top of stable conventions).
   * If omitted or null, false is used.
   */
  internal val experimental: Boolean? = null,
  /**
   * When true, also emit the previous major version alongside the target version.
   * For version=1, the previous version refers to the pre-stable conventions that the instrumentation emitted before the first stable semantic convention version was defined.
   * For version=2 and above, the previous version is the prior stable major version (e.g., version=2, dual_emit=true emits both v2 and v1).
   * Enables dual-emit for phased migration between versions.
   * If omitted or null, false is used.
   */
  @SerialName("dual_emit")
  internal val dualEmit: Boolean? = null,
)
