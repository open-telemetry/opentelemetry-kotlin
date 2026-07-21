// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Long
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BatchSpanProcessor(
  /**
   * Configure delay interval (in milliseconds) between two consecutive exports. 
   * Value must be non-negative.
   * If omitted or null, 5000 is used.
   */
  @SerialName("schedule_delay")
  internal val scheduleDelay: Long? = null,
  /**
   * Configure maximum allowed time (in milliseconds) to export data. 
   * Value must be non-negative. A value of 0 indicates no limit (infinity).
   * If omitted or null, 30000 is used.
   */
  @SerialName("export_timeout")
  internal val exportTimeout: Long? = null,
  /**
   * Configure maximum queue size. Value must be positive.
   * If omitted or null, 2048 is used.
   */
  @SerialName("max_queue_size")
  internal val maxQueueSize: Long? = null,
  /**
   * Configure maximum batch size. Value must be positive.
   * If omitted or null, 512 is used.
   */
  @SerialName("max_export_batch_size")
  internal val maxExportBatchSize: Long? = null,
  /**
   * Configure exporter.
   * Property is required and must be non-null.
   */
  internal val exporter: SpanExporter,
)
