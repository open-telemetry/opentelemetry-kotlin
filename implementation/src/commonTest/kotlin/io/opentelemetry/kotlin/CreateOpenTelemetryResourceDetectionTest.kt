package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResourceDetector
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies that detected resources reach exported telemetry via the public entrypoint, rather than
 * only via the config classes that [io.opentelemetry.kotlin.init.ResourcePrecedenceOrderTest]
 * exercises directly.
 */
@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryResourceDetectionTest {

    private val testKey = "test.key"

    @Test
    fun `detected attributes are included in exported spans and logs`() {
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()

        val api = createOpenTelemetry {
            resourceDetection {
                detector(
                    FakeResourceDetector(
                        name = "test_detector",
                        attributes = mapOf(
                            testKey to "detected",
                            ServiceAttributes.SERVICE_NAME to "detected-service",
                        ),
                    )
                )
            }
            tracerProvider {
                export { spanProcessor }
            }
            loggerProvider {
                export { logProcessor }
            }
        }

        api.tracerProvider.getTracer("test").startSpan("span").end()
        api.loggerProvider.getLogger("test").emit("message")

        val spanResource = spanProcessor.endCalls.single().resource
        assertEquals("detected", spanResource.attributes[testKey])
        assertEquals("detected-service", spanResource.attributes[ServiceAttributes.SERVICE_NAME])

        val logResource = logProcessor.logs.single().resource
        assertEquals("detected", logResource.attributes[testKey])
        assertEquals("detected-service", logResource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun `service name configured in the DSL overrides a detector`() {
        val spanProcessor = FakeSpanProcessor()

        val api = createOpenTelemetry {
            serviceName = "explicit-service"
            resourceDetection {
                detector(FakeResourceDetector(attributes = mapOf(ServiceAttributes.SERVICE_NAME to "detected-service")))
            }
            tracerProvider {
                export { spanProcessor }
            }
        }

        api.tracerProvider.getTracer("test").startSpan("span").end()

        val resource = spanProcessor.endCalls.single().resource
        assertEquals("explicit-service", resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun `a failing detector does not prevent SDK construction`() {
        val handler = FakeSdkErrorHandler()
        val spanProcessor = FakeSpanProcessor()

        val api = createOpenTelemetry {
            errorHandler(handler)
            resourceDetection {
                detector(FakeResourceDetector(name = "broken", error = IllegalStateException("boom")))
                detector(FakeResourceDetector(name = "working", attributes = mapOf(testKey to "detected")))
            }
            tracerProvider {
                export { spanProcessor }
            }
        }

        api.tracerProvider.getTracer("test").startSpan("span").end()

        assertEquals("detected", spanProcessor.endCalls.single().resource.attributes[testKey])
        assertEquals("Resource detector 'broken' failed", handler.userCodeErrors.single().message)
    }
}
