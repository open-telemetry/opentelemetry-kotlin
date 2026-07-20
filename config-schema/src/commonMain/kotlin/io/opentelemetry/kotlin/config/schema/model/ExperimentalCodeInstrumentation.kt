// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalCodeInstrumentation(
  /**
   * Configure code semantic convention version and migration behavior.
   *
   * This property takes precedence over the .instrumentation/development.general.stability_opt_in_list setting.
   *
   * See code semantic conventions: https://opentelemetry.io/docs/specs/semconv/registry/attributes/code/
   * If omitted, uses the general stability_opt_in_list setting, or instrumentations continue emitting their default semantic convention version if not set.
   */
  internal val semconv: ExperimentalSemconvConfig? = null,
)
