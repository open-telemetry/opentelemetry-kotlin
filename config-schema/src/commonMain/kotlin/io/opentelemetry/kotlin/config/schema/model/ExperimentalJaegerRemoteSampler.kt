// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalJaegerRemoteSampler(
  /**
   * Configure the endpoint of the jaeger remote sampling service.
   * Property is required and must be non-null.
   */
  internal val endpoint: String,
  /**
   * Configure the polling interval (in milliseconds) to fetch from the remote sampling service.
   * If omitted or null, 60000 is used.
   */
  internal val interval: Long? = null,
  /**
   * Configure the initial sampler used before first configuration is fetched.
   * Property is required and must be non-null.
   */
  @SerialName("initial_sampler")
  internal val initialSampler: Sampler,
)
