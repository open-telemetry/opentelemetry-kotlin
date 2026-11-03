package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior

/**
 * Captures the batch span processor configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class BatchSpanProcessorConfigDslImpl : BehaviorSupplier<BatchSpanProcessorBehavior> {
    private var exporter: SpanExporterBehavior? = null
    var scheduleDelay: Long? = null
    var exporterTimeout: Long? = null
    var maxQueueSize: Int? = null
    var maxExportBatchSize: Int? = null

    fun exporter(action: SpanExporterConfigDslImpl.() -> Unit) {
        val impl = SpanExporterConfigDslImpl()
        impl.action()
        exporter = impl.toBehavior()
    }

    override fun toBehavior(): BatchSpanProcessorBehavior {
        return BatchSpanProcessorBehavior(
            exporter = exporter,
            scheduleDelay = scheduleDelay,
            exportTimeout = exporterTimeout,
            maxQueueSize = maxQueueSize,
            maxExportBatchSize = maxExportBatchSize,
        )
    }
}
