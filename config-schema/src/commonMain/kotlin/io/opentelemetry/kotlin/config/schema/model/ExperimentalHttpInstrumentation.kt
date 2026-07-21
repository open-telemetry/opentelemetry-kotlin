// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalHttpInstrumentation(
  /**
   * Configure HTTP semantic convention version and migration behavior.
   *
   * This property takes precedence over the .instrumentation/development.general.stability_opt_in_list setting.
   *
   * See HTTP migration: https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/
   * If omitted, uses the general stability_opt_in_list setting, or instrumentations continue emitting their default semantic convention version if not set.
   */
  internal val semconv: ExperimentalSemconvConfig? = null,
  /**
   * Configure instrumentations following the http client semantic conventions.
   * If omitted, defaults as described in ExperimentalHttpClientInstrumentation are used.
   */
  internal val client: ExperimentalHttpClientInstrumentation? = null,
  /**
   * Configure instrumentations following the http server semantic conventions.
   * If omitted, defaults as described in ExperimentalHttpServerInstrumentation are used.
   */
  internal val server: ExperimentalHttpServerInstrumentation? = null,
)
