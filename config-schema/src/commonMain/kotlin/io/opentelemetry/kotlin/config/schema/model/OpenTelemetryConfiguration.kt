// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenTelemetryConfiguration(
  /**
   * The file format version.
   * Represented as a string including the semver major, minor version numbers (and optionally the meta tag). For example: "0.4", "1.0-rc.2", "1.0" (after stable release).
   * See https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md for more details.
   * The yaml format is documented at https://github.com/open-telemetry/opentelemetry-configuration/tree/main/schema
   * Property is required and must be non-null.
   */
  @SerialName("file_format")
  internal val fileFormat: String,
  /**
   * Configure if the SDK is disabled or not.
   * If omitted or null, false is used.
   */
  internal val disabled: Boolean? = null,
  /**
   * Configure the log level of the internal logger used by the SDK.
   * Values include:
   * * debug: debug, severity number 5.
   * * debug2: debug2, severity number 6.
   * * debug3: debug3, severity number 7.
   * * debug4: debug4, severity number 8.
   * * error: error, severity number 17.
   * * error2: error2, severity number 18.
   * * error3: error3, severity number 19.
   * * error4: error4, severity number 20.
   * * fatal: fatal, severity number 21.
   * * fatal2: fatal2, severity number 22.
   * * fatal3: fatal3, severity number 23.
   * * fatal4: fatal4, severity number 24.
   * * info: info, severity number 9.
   * * info2: info2, severity number 10.
   * * info3: info3, severity number 11.
   * * info4: info4, severity number 12.
   * * trace: trace, severity number 1.
   * * trace2: trace2, severity number 2.
   * * trace3: trace3, severity number 3.
   * * trace4: trace4, severity number 4.
   * * warn: warn, severity number 13.
   * * warn2: warn2, severity number 14.
   * * warn3: warn3, severity number 15.
   * * warn4: warn4, severity number 16.
   * If omitted, INFO is used.
   */
  @SerialName("log_level")
  internal val logLevel: SeverityNumber? = null,
  /**
   * Configure general attribute limits. See also tracer_provider.limits, logger_provider.limits.
   * If omitted, default values as described in AttributeLimits are used.
   */
  @SerialName("attribute_limits")
  internal val attributeLimits: AttributeLimits? = null,
  /**
   * Configure logger provider.
   * If omitted, a noop logger provider is used.
   */
  @SerialName("logger_provider")
  internal val loggerProvider: LoggerProvider? = null,
  /**
   * Configure meter provider.
   * If omitted, a noop meter provider is used.
   */
  @SerialName("meter_provider")
  internal val meterProvider: MeterProvider? = null,
  /**
   * Configure text map context propagators.
   * If omitted, a noop propagator is used.
   */
  internal val propagator: Propagator? = null,
  /**
   * Configure tracer provider.
   * If omitted, a noop tracer provider is used.
   */
  @SerialName("tracer_provider")
  internal val tracerProvider: TracerProvider? = null,
  /**
   * Configure resource for all signals.
   * If omitted, the default resource is used.
   */
  internal val resource: Resource? = null,
  /**
   * Configure instrumentation.
   * If omitted, instrumentation defaults are used.
   */
  @SerialName("instrumentation/development")
  internal val instrumentationDevelopment: ExperimentalInstrumentation? = null,
  /**
   * Defines configuration parameters specific to a particular OpenTelemetry distribution or vendor.
   * This section provides a standardized location for distribution-specific settings
   * that are not part of the OpenTelemetry configuration model.
   * It allows vendors to expose their own extensions and general configuration options.
   * If omitted, distribution defaults are used.
   */
  internal val distribution: Distribution? = null,
)
