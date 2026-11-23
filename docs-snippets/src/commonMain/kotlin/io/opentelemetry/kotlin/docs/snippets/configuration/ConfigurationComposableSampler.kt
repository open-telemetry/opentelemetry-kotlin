package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.sampling.SamplingPredicate

// region configuration-composable-sampler
private val isHealthCheck = SamplingPredicate { _, name, _, _, _ -> name == "GET /health" }
private val anySpan = SamplingPredicate { _, _, _, _, _ -> true }

fun initWithRuleBasedSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        sampler {
            composite {
                composableParentThreshold(
                    root = composableRuleBased {
                        rule(isHealthCheck) { composableAlwaysOff() } // the first matching rule wins
                        rule(anySpan) { composableProbability(0.25) }
                    }
                )
            }
        }
    }
}
// end-region
