package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier

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
            scheduleDelay = scheduleDelay?.takeIf { it >= 0 },
            exportTimeout = exporterTimeout?.takeIf { value -> value >= 0 },
            maxQueueSize = maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() },
            maxExportBatchSize = maxExportBatchSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() },
        )
    }
}
