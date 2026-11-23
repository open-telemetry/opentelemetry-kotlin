package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class CreateCompatOpenTelemetryErrorHandlerTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `throwing config lambda does not escape and keeps the config applied before it threw`() {
        val handler = FakeSdkErrorHandler()
        val processor = FakeSpanProcessor()
        val api = createCompatOpenTelemetry {
            errorHandler(handler)
            tracerProvider {
                export { processor }
            }
            error("boom")
        }.also(sdks::track)

        api.tracerProvider.getTracer("test").startSpan("span").end()

        assertEquals(1, processor.endCalls.size)
        val error = handler.userCodeErrors.single()
        assertEquals("Failed to apply OpenTelemetry config, continuing with partial config", error.message)
        assertEquals("boom", error.cause.message)
    }

    @Test
    fun `reports below the resolved log level are dropped`() {
        assertEquals(1, processorErrorsDelivered(logLevel = null))
        assertEquals(0, processorErrorsDelivered(logLevel = SeverityLevel.ERROR))
    }

    private fun processorErrorsDelivered(logLevel: SeverityLevel?): Int {
        val handler = FakeSdkErrorHandler()
        val api = createCompatOpenTelemetry {
            errorHandler(handler)
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(logLevel = logLevel) }, "otel.yaml")
            tracerProvider {
                export { FakeSpanProcessor(endAction = { throw IllegalStateException("boom") }) }
            }
        }.also(sdks::track)
        api.tracerProvider.getTracer("test").startSpan("span").end()
        return handler.userCodeErrors.count { it.severity == SdkErrorSeverity.WARNING }
    }
}
