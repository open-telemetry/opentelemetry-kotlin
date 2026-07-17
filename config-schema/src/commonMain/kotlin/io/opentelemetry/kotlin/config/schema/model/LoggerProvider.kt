// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoggerProvider(
  /**
   * Configure log record processors.
   * Property is required and must be non-null.
   */
  internal val processors: List<LogRecordProcessor>,
  /**
   * Configure log record limits. See also attribute_limits.
   * If omitted, default values as described in LogRecordLimits are used.
   */
  internal val limits: LogRecordLimits? = null,
  /**
   * Configure loggers.
   * If omitted, all loggers use default values as described in ExperimentalLoggerConfig.
   */
  @SerialName("logger_configurator/development")
  internal val loggerConfiguratorDevelopment: ExperimentalLoggerConfigurator? = null,
)
