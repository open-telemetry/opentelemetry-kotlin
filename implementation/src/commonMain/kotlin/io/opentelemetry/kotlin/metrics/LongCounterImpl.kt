package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.metrics.export.LongMetricRecord
import io.opentelemetry.kotlin.metrics.export.MetricProcessor

internal class LongCounterImpl(
    override val name: String,
    override val description: String?,
    override val unit: String?,
    private val contextFactory: ContextFactory,
    private val processor: MetricProcessor?,
    private val shutdownState: ShutdownState,
) : LongCounter {

    override fun add(
        value: Long,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        shutdownState.execute {
            val record: LongMetricRecord = LongMetricRecordImpl(
                name,
                "counter",
                value,
                unit,
                description
            )
            attributes?.invoke(record)
            val ctx = context ?: contextFactory.implicit()
            processor?.onEmit(record, ctx)
        }
    }

    override fun enabled(): Boolean =
        if (shutdownState.isShutdown || processor == null) {
            false
        } else {
            val ctx = contextFactory.implicit()
            processor.enabled(ctx, name)
        }
}
