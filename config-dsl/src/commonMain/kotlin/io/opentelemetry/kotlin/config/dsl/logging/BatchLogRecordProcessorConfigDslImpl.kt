package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior

/**
 * Captures the batch log record processor configured programmatically and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class BatchLogRecordProcessorConfigDslImpl : BehaviorSupplier<BatchLogRecordProcessorBehavior> {
    private var exporter: LogRecordExporterBehavior? = null
    var scheduleDelay: Long? = null
    var exporterTimeout: Long? = null
    var maxQueueSize: Int? = null
    var requestedQueueSize: Int? = null

    fun exporter(action: LogRecordExporterConfigDslImpl.() -> Unit) {
        val impl = LogRecordExporterConfigDslImpl()
        impl.action()
        exporter = impl.toBehavior()
    }

    override fun toBehavior(): BatchLogRecordProcessorBehavior {
        return BatchLogRecordProcessorBehavior(
            exporter = exporter,
            scheduleDelay = scheduleDelay?.takeIf { it >= 0 },
            exportTimeout = exporterTimeout?.takeIf { value -> value >= 0 },
            maxQueueSize = maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() },
            requestedQueueSize = requestedQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() },
        )
    }
}
