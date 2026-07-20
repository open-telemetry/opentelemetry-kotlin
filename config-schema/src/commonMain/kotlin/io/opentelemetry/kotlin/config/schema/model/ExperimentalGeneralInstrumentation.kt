// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalGeneralInstrumentation(
  /**
   * Configure instrumentations following the http semantic conventions.
   * See http semantic conventions: https://opentelemetry.io/docs/specs/semconv/http/
   * If omitted, defaults as described in ExperimentalHttpInstrumentation are used.
   */
  internal val http: ExperimentalHttpInstrumentation? = null,
  /**
   * Configure instrumentations following the code semantic conventions.
   * See code semantic conventions: https://opentelemetry.io/docs/specs/semconv/registry/attributes/code/
   * If omitted, defaults as described in ExperimentalCodeInstrumentation are used.
   */
  internal val code: ExperimentalCodeInstrumentation? = null,
  /**
   * Configure instrumentations following the database semantic conventions.
   * See database semantic conventions: https://opentelemetry.io/docs/specs/semconv/database/
   * If omitted, defaults as described in ExperimentalDbInstrumentation are used.
   */
  internal val db: ExperimentalDbInstrumentation? = null,
  /**
   * Configure instrumentations following the GenAI semantic conventions.
   * See GenAI semantic conventions: https://opentelemetry.io/docs/specs/semconv/gen-ai/
   * If omitted, defaults as described in ExperimentalGenAiInstrumentation are used.
   */
  @SerialName("gen_ai")
  internal val genAi: ExperimentalGenAiInstrumentation? = null,
  /**
   * Configure instrumentations following the messaging semantic conventions.
   * See messaging semantic conventions: https://opentelemetry.io/docs/specs/semconv/messaging/
   * If omitted, defaults as described in ExperimentalMessagingInstrumentation are used.
   */
  internal val messaging: ExperimentalMessagingInstrumentation? = null,
  /**
   * Configure instrumentations following the RPC semantic conventions.
   * See RPC semantic conventions: https://opentelemetry.io/docs/specs/semconv/rpc/
   * If omitted, defaults as described in ExperimentalRpcInstrumentation are used.
   */
  internal val rpc: ExperimentalRpcInstrumentation? = null,
  /**
   * Configure general sanitization options.
   * If omitted, defaults as described in ExperimentalSanitization are used.
   */
  internal val sanitization: ExperimentalSanitization? = null,
  /**
   * Configure semantic convention stability opt-in as a comma-separated list.
   * This property follows the format and semantics of the OTEL_SEMCONV_STABILITY_OPT_IN environment variable.
   * Controls the emission of stable vs. experimental semantic conventions for instrumentation.
   * This setting is only intended for migrating from experimental to stable semantic conventions.
   *
   * Known values include:
   * - http: Emit stable HTTP and networking conventions only
   * - http/dup: Emit both old and stable HTTP and networking conventions (for phased migration)
   * - database: Emit stable database conventions only
   * - database/dup: Emit both old and stable database conventions (for phased migration)
   * - rpc: Emit stable RPC conventions only
   * - rpc/dup: Emit both experimental and stable RPC conventions (for phased migration)
   * - messaging: Emit stable messaging conventions only
   * - messaging/dup: Emit both old and stable messaging conventions (for phased migration)
   * - code: Emit stable code conventions only
   * - code/dup: Emit both old and stable code conventions (for phased migration)
   *
   * Multiple values can be specified as a comma-separated list (e.g., "http,database/dup").
   * Additional signal types may be supported in future versions.
   *
   * Domain-specific semconv properties (e.g., .instrumentation/development.general.db.semconv) take precedence over this general setting.
   *
   * See:
   * - HTTP migration: https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/
   * - Database migration: https://opentelemetry.io/docs/specs/semconv/database/
   * - RPC: https://opentelemetry.io/docs/specs/semconv/rpc/
   * - Messaging: https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/
   * If omitted or null, no opt-in is configured and instrumentations continue emitting their default semantic convention version.
   */
  @SerialName("stability_opt_in_list")
  internal val stabilityOptInList: String? = null,
)
