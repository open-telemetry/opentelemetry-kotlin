package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingIntent
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.kotlin.tracing.model.TraceStateAdapter
import java.util.function.Function

/** Sentinel used when the delegate's intent is `null` (never sample); `R` is always < 2^56. */
private const val NEVER_SAMPLE_THRESHOLD: Long = 1L shl 56

internal class KotlinComposableSamplerAdapter(private val delegate: ComposableSampler) : OtelJavaComposableSampler {

    override fun getSamplingIntent(
        context: OtelJavaContext,
        traceId: String,
        name: String,
        spanKind: OtelJavaSpanKind,
        attributes: OtelJavaAttributes,
        links: List<OtelJavaLinkData>,
    ): OtelJavaSamplingIntent {
        val intent = delegate.getSamplingIntent(
            context.toOtelKotlinContext(),
            name,
            spanKind.toOtelKotlinSpanKind(),
            CompatAttributesModel(attributes.toBuilder()),
            emptyList(),
        )

        val threshold = intent.threshold ?: NEVER_SAMPLE_THRESHOLD
        val javaAttributes = (intent.attributesProvider?.invoke() as? CompatAttributesModel)
            ?.otelJavaAttributes()
            ?: OtelJavaAttributes.empty()
        val traceStateUpdater: Function<OtelJavaTraceState, OtelJavaTraceState>? =
            intent.traceStateProvider?.let { provider ->
                Function { javaTraceState ->
                    provider(TraceStateAdapter(javaTraceState), SamplingResult.Decision.RECORD_AND_SAMPLE)
                        .toOtelJavaTraceState()
                }
            }

        return OtelJavaSamplingIntent.create(threshold, intent.adjustedCountReliable, javaAttributes, traceStateUpdater)
    }

    override fun getDescription(): String = delegate.description
}
