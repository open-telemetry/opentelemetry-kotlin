package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.toOtelJavaAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinTraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class OtelJavaBackedComposableSampler(
    internal val impl: OtelJavaComposableSampler,
    private val sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
) : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingIntent = sdkErrorHandler.sdkGuardOrDefault(
        fallbackIntent,
        "ComposableSampler.getSamplingIntent failed",
    ) {
        val traceId = context.extractSpan().spanContext.traceId

        val javaIntent = impl.getSamplingIntent(
            context.toOtelJavaContext(),
            traceId,
            name,
            spanKind.toOtelJavaSpanKind(),
            attributes.toOtelJavaAttributes(),
            links.map { it.toOtelJavaLinkData() },
        )

        val javaThreshold = javaIntent.threshold
        val javaTraceStateUpdater = javaIntent.traceStateUpdater
        val javaSamplingAttributes = javaIntent.attributes
        val javaThresholdReliable = javaIntent.isThresholdReliable

        object : SamplingIntent {
            override val threshold: Long? =
                javaThreshold.takeIf { it in VALID_THRESHOLD_RANGE }

            override val adjustedCountReliable: Boolean =
                javaThresholdReliable

            override val attributesProvider: (() -> AttributeContainer)? =
                if (javaSamplingAttributes.isEmpty) {
                    null
                } else {
                    { CompatAttributesModel(javaSamplingAttributes.toBuilder()) }
                }

            override val traceStateProvider:
                ((TraceState, SamplingResult.Decision) -> TraceState)? =
                javaTraceStateUpdater?.let { updater ->
                    {
                            traceState, _ ->
                        sdkErrorHandler.sdkGuardOrDefault(
                            traceState,
                            "ComposableSampler.traceStateProvider failed",
                        ) {
                            updater.apply(
                                traceState.toOtelJavaTraceState()
                            ).toOtelKotlinTraceState()
                        }
                    }
                }
        }
    }

    override val description: String
        get() = impl.description

    companion object {
        /** Valid thresholds lie in `[0, 2^56)`; anything outside that range means "never sample". */
        private const val MAX_THRESHOLD: Long = 1L shl 56
        private val VALID_THRESHOLD_RANGE = 0 until MAX_THRESHOLD

        private val fallbackIntent = object : SamplingIntent {
            override val threshold: Long? = null
            override val adjustedCountReliable: Boolean = false
            override val attributesProvider: (() -> AttributeContainer)? = null
            override val traceStateProvider:
                ((TraceState, SamplingResult.Decision) -> TraceState)? = null
        }
    }
}
