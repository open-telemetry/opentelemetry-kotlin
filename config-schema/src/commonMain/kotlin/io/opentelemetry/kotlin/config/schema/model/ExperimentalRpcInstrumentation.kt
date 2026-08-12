// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalRpcInstrumentation(
  /**
   * Configure RPC semantic convention version and migration behavior.
   *
   * This property takes precedence over the .instrumentation/development.general.stability_opt_in_list setting.
   *
   * See RPC semantic conventions: https://opentelemetry.io/docs/specs/semconv/rpc/
   * If omitted, uses the general stability_opt_in_list setting, or instrumentations continue emitting their default semantic convention version if not set.
   */
  public val semconv: ExperimentalSemconvConfig? = null,
)
