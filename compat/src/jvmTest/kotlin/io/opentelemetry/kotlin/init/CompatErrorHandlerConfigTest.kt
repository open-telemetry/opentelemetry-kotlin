package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class CompatErrorHandlerConfigTest {

    private val clock = FakeClock()

    @Test
    fun `configured error handler receives reports`() {
        val handler = FakeSdkErrorHandler()
        val captured = captureExportErrorHandler {
            errorHandler(handler)
        }
        captured.onError(sdkError())
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun `error handler configured after the export block still receives reports`() {
        val handler = FakeSdkErrorHandler()
        val captured = captureExportErrorHandler(configureHandlerFirst = false) {
            errorHandler(handler)
        }
        captured.onError(sdkError())
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun `reports are discarded when no error handler is configured`() {
        val captured = captureExportErrorHandler { }
        captured.onError(sdkError())
    }

    /**
     * Builds a compat config, capturing the [SdkErrorHandler] handed to the `export` block. When
     * [configureHandlerFirst] is false the handler is configured after that block has already run.
     */
    private fun captureExportErrorHandler(
        configureHandlerFirst: Boolean = true,
        configure: CompatOpenTelemetryConfig.() -> Unit,
    ): SdkErrorHandler {
        var captured: SdkErrorHandler? = null
        val cfg = CompatOpenTelemetryConfig(clock)
        if (configureHandlerFirst) {
            cfg.configure()
        }
        cfg.tracerProvider {
            export {
                captured = sdkErrorHandler
                FakeSpanProcessor()
            }
        }
        if (!configureHandlerFirst) {
            cfg.configure()
        }
        return assertNotNull(captured)
    }

    private fun sdkError() = SdkError.ApiMisuse("TestApi", "boom", SdkErrorSeverity.WARNING)
}
