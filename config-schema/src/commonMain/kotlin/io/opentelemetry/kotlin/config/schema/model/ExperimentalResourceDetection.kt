// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalResourceDetection(
  /**
   * Configure attributes provided by resource detectors.
   * If omitted, all attributes from resource detectors are added.
   */
  internal val attributes: IncludeExclude? = null,
  /**
   * Configure resource detectors.
   * Resource detector names are dependent on the SDK language ecosystem. Please consult documentation for each respective language. 
   * If omitted, no resource detectors are enabled.
   */
  internal val detectors: List<ExperimentalResourceDetector>? = null,
)
