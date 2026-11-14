package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class RemoteSamplerEntrypointTest {

    @Test
    fun jaegerStrategyControlsTheSdkSampler() = runTest {
        val errors = FakeSdkErrorHandler()
        val sdk = createOpenTelemetry {
            tracerProvider {
                export { FakeSpanProcessor() }
                sampler {
                    remoteSamplerFromJson(
                        """
                        {
                          "operationSampling": {
                            "defaultSamplingProbability": 0,
                            "perOperationStrategies": [
                              {"operation": "/sampled", "probabilisticSampling": {"samplingRate": 1}}
                            ]
                          }
                        }
                        """.trimIndent(),
                        errors,
                    ) ?: alwaysOff()
                }
            }
        }
        try {
            val tracer = sdk.tracerProvider.getTracer("test")
            val sampled = tracer.startSpan("/sampled", parentContext = rootContext())
            val dropped = tracer.startSpan("/other", parentContext = rootContext())
            sampled.end()
            dropped.end()

            assertTrue(sampled.spanContext.traceFlags.isSampled)
            assertFalse(dropped.spanContext.traceFlags.isSampled)
            assertFalse(errors.hasErrors())
        } finally {
            sdk.shutdown()
        }
    }
}
