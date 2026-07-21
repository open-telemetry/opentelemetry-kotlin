// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TextMapPropagator(
  /**
   * Include the w3c trace context propagator.
   * If omitted, ignore.
   */
  internal val tracecontext: TraceContextPropagator? = null,
  /**
   * Include the w3c baggage propagator.
   * If omitted, ignore.
   */
  internal val baggage: BaggagePropagator? = null,
  /**
   * Include the zipkin b3 propagator.
   * If omitted, ignore.
   */
  internal val b3: B3Propagator? = null,
  /**
   * Include the zipkin b3 multi propagator.
   * If omitted, ignore.
   */
  internal val b3multi: B3MultiPropagator? = null,
)
