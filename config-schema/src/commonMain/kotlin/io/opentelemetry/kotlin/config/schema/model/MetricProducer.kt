// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class MetricProducer(
  /**
   * Configure metric producer to be opencensus.
   *
   * **Deprecated** as of v1.2.0, may be removed in v2.0.0. The OpenCensus
   * compatibility specification it relies on was deprecated in
   * https://github.com/open-telemetry/opentelemetry-specification/pull/5138.
   * SDKs MAY continue to support this entry for backwards compatibility;
   * new configurations SHOULD NOT use it.
   * If omitted, ignore.
   */
  public val opencensus: OpenCensusMetricProducer? = null,
)
