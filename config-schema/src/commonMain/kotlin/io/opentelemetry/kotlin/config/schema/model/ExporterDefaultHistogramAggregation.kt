// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ExporterDefaultHistogramAggregation {
  @SerialName("explicit_bucket_histogram")
  EXPLICIT_BUCKET_HISTOGRAM,
  @SerialName("base2_exponential_bucket_histogram")
  BASE2_EXPONENTIAL_BUCKET_HISTOGRAM,
}
