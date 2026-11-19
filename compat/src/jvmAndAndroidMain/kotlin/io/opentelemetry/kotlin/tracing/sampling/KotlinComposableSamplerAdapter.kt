package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingIntent
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.toOtelJavaAttributes
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanLink
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinTraceState
import java.util.function.BiFunction

/** Sentinel used when the delegate's intent is `null` (never sample); `R` is always < 2^56. */
private const val NEVER_SAMPLE_THRESHOLD: Long = 1L shl 56

internal class KotlinComposableSamplerAdapter(private val delegate: ComposableSampler) : OtelJavaComposableSampler {

    override fun getSamplingIntent(
        context: OtelJavaContext,
        name: String,
        spanKind: OtelJavaSpanKind,
        attributes: OtelJavaAttributes,
        links: List<OtelJavaLinkData>,
    ): OtelJavaSamplingIntent {
        val kotlinContext = context.toOtelKotlinContext()
        val kotlinSpanKind = spanKind.toOtelKotlinSpanKind()
        val kotlinAttributes = CompatAttributesModel(attributes.toBuilder())
        val kotlinLinks = links.map { it.toOtelKotlinSpanLink() }

        val intent = userCode {
            delegate.getSamplingIntent(
                kotlinContext,
                name,
                kotlinSpanKind,
                kotlinAttributes,
                kotlinLinks,
            )
        }

        val threshold = userCode { intent.threshold } ?: NEVER_SAMPLE_THRESHOLD
        val adjustedCountReliable = userCode { intent.adjustedCountReliable }

        val attributesProvider = userCode { intent.attributesProvider }

        val providedAttributes = attributesProvider?.let { provider ->
            userCode { provider() }
        }

        val javaAttributes = when (providedAttributes) {
            null -> {
                OtelJavaAttributes.empty()
            }

            is CompatAttributesModel -> {
                providedAttributes.toOtelJavaAttributes()
            }

            else -> {
                val attributeMap = userCode {
                    providedAttributes.attributes
                }
                attrsFromMap(attributeMap)
            }
        }

        val traceStateProvider = userCode { intent.traceStateProvider }

        val traceStateUpdater: BiFunction<OtelJavaTraceState, OtelJavaSamplingDecision, OtelJavaTraceState> =
            if (traceStateProvider == null) {
                BiFunction { state, _ -> state }
            } else {
                BiFunction { javaTraceState, decision ->
                    val kotlinTraceState =
                        javaTraceState.toOtelKotlinTraceState()

                    val updatedTraceState = userCode {
                        traceStateProvider(
                            kotlinTraceState,
                            SamplingResult.Decision.valueOf(decision.name),
                        )
                    }

                    val traceStateEntries = userCode {
                        updatedTraceState.asMap()
                    }

                    traceStateEntries.toOtelJavaTraceState()
                }
            }

        return OtelJavaSamplingIntent.create(
            threshold,
            adjustedCountReliable,
            javaAttributes,
            traceStateUpdater,
        )
    }

    override fun getDescription(): String = delegate.description
}
