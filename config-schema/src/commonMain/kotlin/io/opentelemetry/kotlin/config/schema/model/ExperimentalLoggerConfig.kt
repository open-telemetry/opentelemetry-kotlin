// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalLoggerConfig(
  /**
   * Configure if the logger is enabled or not.
   * If omitted or null, true is used.
   */
  internal val enabled: Boolean? = null,
  /**
   * Configure severity filtering.
   * Log records with an non-zero (i.e. unspecified) severity number which is less than minimum_severity are not processed.
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
   * If omitted, severity filtering is not applied.
   */
  @SerialName("minimum_severity")
  internal val minimumSeverity: SeverityNumber? = null,
  /**
   * Configure trace based filtering.
   * If true, log records associated with unsampled trace contexts traces are not processed. If false, or if a log record is not associated with a trace context, trace based filtering is not applied.
   * If omitted or null, trace based filtering is not applied.
   */
  @SerialName("trace_based")
  internal val traceBased: Boolean? = null,
)
