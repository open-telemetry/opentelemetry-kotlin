// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalTracerConfigurator(
  /**
   * Configure the default tracer config used there is no matching entry in .tracer_configurator/development.tracers.
   * If omitted, unmatched .tracers use default values as described in ExperimentalTracerConfig.
   */
  @SerialName("default_config")
  public val defaultConfig: ExperimentalTracerConfig? = null,
  /**
   * Configure tracers.
   * If omitted, all tracers use .default_config.
   */
  public val tracers: List<ExperimentalTracerMatcherAndConfig>? = null,
)
