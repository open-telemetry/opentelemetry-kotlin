// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class View(
  /**
   * Configure view selector. 
   * Selection criteria is additive as described in https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#instrument-selection-criteria.
   * Property is required and must be non-null.
   */
  internal val selector: ViewSelector,
  /**
   * Configure view stream.
   * Property is required and must be non-null.
   */
  internal val stream: ViewStream,
)
