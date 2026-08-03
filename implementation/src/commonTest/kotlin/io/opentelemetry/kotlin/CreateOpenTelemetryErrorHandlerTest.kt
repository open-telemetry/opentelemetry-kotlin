package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryErrorHandlerTest {

    @Test
    fun `handler configured via the DSL receives errors thrown by user code`() = runTest {
        val handler = FakeSdkErrorHandler()
        val api = createOpenTelemetry {
            errorHandler(handler)
            tracerProvider {
                export { throwingProcessor() }
            }
        }

        val result = tracerCloseable(api).shutdown()

        assertEquals(OperationResultCode.Failure, result)
        val error = handler.userCodeErrors.single()
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }

    @Test
    fun `handler is reached when configured after the signal it reports for`() = runTest {
        val handler = FakeSdkErrorHandler()
        val api = createOpenTelemetry {
            tracerProvider {
                export { throwingProcessor() }
            }
            errorHandler(handler)
        }

        tracerCloseable(api).shutdown()

        assertTrue(handler.hasErrors())
    }

    @Test
    fun `errors are discarded when no handler is configured`() = runTest {
        val handler = FakeSdkErrorHandler()
        val api = createOpenTelemetry {
            tracerProvider {
                export { throwingProcessor() }
            }
        }

        // the throwing processor must not surface as an exception to the caller
        assertEquals(OperationResultCode.Failure, tracerCloseable(api).shutdown())
        assertFalse(handler.hasErrors())
    }

    private fun throwingProcessor() = FakeSpanProcessor(
        shutdownCode = { throw IllegalStateException("boom") }
    )

    private fun tracerCloseable(api: OpenTelemetry) = api.tracerProvider as TelemetryCloseable
}
