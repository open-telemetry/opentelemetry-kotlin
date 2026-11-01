package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class CreateCompatOpenTelemetryErrorHandlerTest {

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
        }

        api.tracerProvider.getTracer("test").startSpan("span").end()

        assertEquals(1, processor.endCalls.size)
        val error = handler.userCodeErrors.single()
        assertEquals("Failed to apply OpenTelemetry config, continuing with partial config", error.message)
        assertEquals("boom", error.cause.message)
    }
}
