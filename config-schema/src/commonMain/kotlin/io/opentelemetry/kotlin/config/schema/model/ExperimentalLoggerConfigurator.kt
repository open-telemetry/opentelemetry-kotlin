// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalLoggerConfigurator(
  /**
   * Configure the default logger config used there is no matching entry in .logger_configurator/development.loggers.
   * If omitted, unmatched .loggers use default values as described in ExperimentalLoggerConfig.
   */
  @SerialName("default_config")
  internal val defaultConfig: ExperimentalLoggerConfig? = null,
  /**
   * Configure loggers.
   * If omitted, all loggers use .default_config.
   */
  internal val loggers: List<ExperimentalLoggerMatcherAndConfig>? = null,
)
