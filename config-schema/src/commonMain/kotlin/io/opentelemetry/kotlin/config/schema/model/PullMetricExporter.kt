// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PullMetricExporter(
  /**
   * Configure exporter to be prometheus.
   * If omitted, ignore.
   */
  @SerialName("prometheus/development")
  internal val prometheusDevelopment: ExperimentalPrometheusMetricExporter? = null,
)
