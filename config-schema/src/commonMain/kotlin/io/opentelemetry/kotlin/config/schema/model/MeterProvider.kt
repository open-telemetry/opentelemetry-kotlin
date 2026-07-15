// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MeterProvider(
  /**
   * Configure metric readers.
   * Property is required and must be non-null.
   */
  internal val readers: List<MetricReader>,
  /**
   * Configure views. 
   * Each view has a selector which determines the instrument(s) it applies to, and a configuration for the resulting stream(s).
   * If omitted, no views are registered.
   */
  internal val views: List<View>? = null,
  /**
   * Configure the exemplar filter.
   * Values include:
   * * always_off: ExemplarFilter which makes no measurements eligible for being an Exemplar.
   * * always_on: ExemplarFilter which makes all measurements eligible for being an Exemplar.
   * * trace_based: ExemplarFilter which makes measurements recorded in the context of a sampled parent span eligible for being an Exemplar.
   * If omitted, trace_based is used.
   */
  @SerialName("exemplar_filter")
  internal val exemplarFilter: ExemplarFilter? = null,
  /**
   * Configure meters.
   * If omitted, all meters use default values as described in ExperimentalMeterConfig.
   */
  @SerialName("meter_configurator/development")
  internal val meterConfiguratorDevelopment: ExperimentalMeterConfigurator? = null,
)
