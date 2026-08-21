// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalMeterConfigurator(
  /**
   * Configure the default meter config used there is no matching entry in .meter_configurator/development.meters.
   * If omitted, unmatched .meters use default values as described in ExperimentalMeterConfig.
   */
  @SerialName("default_config")
  public val defaultConfig: ExperimentalMeterConfig? = null,
  /**
   * Configure meters.
   * If omitted, all meters used .default_config.
   */
  public val meters: List<ExperimentalMeterMatcherAndConfig>? = null,
)
