package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior

/**
 * Captures the batch log record processor configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class BatchLogRecordProcessorConfigDslImpl : BehaviorSupplier<BatchLogRecordProcessorBehavior> {
    private var exporter: LogRecordExporterBehavior? = null
    var scheduleDelay: Long? = null
    var exporterTimeout: Long? = null
    var maxQueueSize: Int? = null
    var maxExportBatchSize: Int? = null

    fun exporter(action: LogRecordExporterConfigDslImpl.() -> Unit) {
        val impl = LogRecordExporterConfigDslImpl()
        impl.action()
        exporter = impl.toBehavior()
    }

    override fun toBehavior(): BatchLogRecordProcessorBehavior {
        return BatchLogRecordProcessorBehavior(
            exporter = exporter,
            scheduleDelay = scheduleDelay,
            exportTimeout = exporterTimeout,
            maxQueueSize = maxQueueSize,
            maxExportBatchSize = maxExportBatchSize,
        )
    }
}
