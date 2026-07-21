// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TracerProvider(
  /**
   * Configure span processors.
   * Property is required and must be non-null.
   */
  internal val processors: List<SpanProcessor>,
  /**
   * Configure span limits. See also attribute_limits.
   * If omitted, default values as described in SpanLimits are used.
   */
  internal val limits: SpanLimits? = null,
  /**
   * Configure the sampler.
   * If omitted, parent based sampler with a root of always_on is used.
   */
  internal val sampler: Sampler? = null,
  /**
   * Configure the trace and span ID generator.
   * If omitted, RandomIdGenerator is used.
   */
  @SerialName("id_generator")
  internal val idGenerator: IdGenerator? = null,
  /**
   * Configure tracers.
   * If omitted, all tracers use default values as described in ExperimentalTracerConfig.
   */
  @SerialName("tracer_configurator/development")
  internal val tracerConfiguratorDevelopment: ExperimentalTracerConfigurator? = null,
)
