// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalPrometheusMetricExporter(
  /**
   * Configure host.
   * If omitted or null, localhost is used.
   */
  public val host: String? = null,
  /**
   * Configure port.
   * If omitted or null, 9464 is used.
   */
  public val port: Long? = null,
  /**
   * Configure Prometheus Exporter to produce metrics with scope labels.
   * If omitted or null, true is used.
   */
  @SerialName("scope_info_enabled")
  public val scopeInfoEnabled: Boolean? = null,
  /**
   * Configure Prometheus Exporter to produce metrics with a target info metric for the resource.
   * If omitted or null, true is used.
   */
  @SerialName("target_info_enabled/development")
  public val targetInfoEnabledDevelopment: Boolean? = null,
  /**
   * Configure Prometheus Exporter to add resource attributes as metrics attributes, where the resource attribute keys match the patterns.
   * If omitted, no resource attributes are added.
   */
  @SerialName("resource_constant_labels")
  public val resourceConstantLabels: IncludeExclude? = null,
  /**
   * Configure how metric names are translated to Prometheus metric names.
   * Values include:
   * * no_translation/development: Special character escaping is disabled. Type and unit suffixes are disabled. Metric names are unaltered.
   * * no_utf8_escaping_with_suffixes/development: Special character escaping is disabled. Type and unit suffixes are enabled.
   * * underscore_escaping_with_suffixes: Special character escaping is enabled. Type and unit suffixes are enabled.
   * * underscore_escaping_without_suffixes/development: Special character escaping is enabled. Type and unit suffixes are disabled. This represents classic Prometheus metric name compatibility.
   * If omitted, underscore_escaping_with_suffixes is used.
   */
  @SerialName("translation_strategy")
  public val translationStrategy: ExperimentalPrometheusTranslationStrategy? = null,
)
