// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LogRecordProcessor(
  /**
   * Configure a batch log record processor.
   * If omitted, ignore.
   */
  internal val batch: BatchLogRecordProcessor? = null,
  /**
   * Configure a simple log record processor.
   * If omitted, ignore.
   */
  internal val simple: SimpleLogRecordProcessor? = null,
  /**
   * Configure an event to span event bridge log record processor.
   * If omitted, ignore.
   */
  @SerialName("event_to_span_event_bridge/development")
  internal val eventToSpanEventBridgeDevelopment:
      ExperimentalEventToSpanEventBridgeLogRecordProcessor? = null,
)
