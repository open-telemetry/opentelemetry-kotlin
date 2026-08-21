// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class MetricProducer(
  /**
   * Configure metric producer to be opencensus.
   * If omitted, ignore.
   */
  public val opencensus: OpenCensusMetricProducer? = null,
)
