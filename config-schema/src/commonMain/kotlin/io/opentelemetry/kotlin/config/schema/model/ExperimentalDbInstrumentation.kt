// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalDbInstrumentation(
  /**
   * Configure database semantic convention version and migration behavior.
   *
   * This property takes precedence over the .instrumentation/development.general.stability_opt_in_list setting.
   *
   * See database migration: https://opentelemetry.io/docs/specs/semconv/database/
   * If omitted, uses the general stability_opt_in_list setting, or instrumentations continue emitting their default semantic convention version if not set.
   */
  internal val semconv: ExperimentalSemconvConfig? = null,
)
