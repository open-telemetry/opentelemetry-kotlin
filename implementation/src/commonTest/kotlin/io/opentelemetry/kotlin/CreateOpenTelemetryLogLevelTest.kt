package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryLogLevelTest {

    private val sdks = SdkTracker()
    private val handler = FakeSdkErrorHandler()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `warnings are delivered by default`() = runTest {
        assertEquals(1, warningsDelivered { })
    }

    @Test
    fun `DSL log level drops reports below it`() = runTest {
        assertEquals(0, warningsDelivered { logLevel(SeverityNumber.ERROR) })
    }

    @Test
    fun `WARN log level delivers warnings`() = runTest {
        assertEquals(1, warningsDelivered { logLevel(SeverityNumber.WARN) })
    }

    @Test
    fun `config file log level drops reports below it`() = runTest {
        val delivered = warningsDelivered {
            configFile(fileWithLogLevel(SeverityLevel.FATAL), "otel.yaml")
        }
        assertEquals(0, delivered)
    }

    @Test
    fun `DSL log level takes precedence over the config file`() = runTest {
        val delivered = warningsDelivered {
            configFile(fileWithLogLevel(SeverityLevel.ERROR), "otel.yaml")
            logLevel(SeverityNumber.DEBUG)
        }
        assertEquals(1, delivered)
    }

    @Test
    fun `DSL log level filters reports made later in the config block`() {
        createOpenTelemetry {
            errorHandler(handler)
            logLevel(SeverityNumber.ERROR)
            shutdownTimeout(-1)
        }.also(sdks::track)

        assertTrue(handler.errors.none { it.severity < SdkErrorSeverity.ERROR })
    }

    @Test
    fun `unknown DSL log level is reported and ignored`() = runTest {
        assertEquals(1, warningsDelivered { logLevel(SeverityNumber.UNKNOWN) })
        assertEquals("OpenTelemetryConfigDsl.logLevel", handler.apiMisuses.single().api)
    }

    /**
     * Creates an SDK whose span processor fails on shutdown, which the SDK reports as a warning, and
     * returns how many of those warnings reached the handler.
     */
    private suspend fun warningsDelivered(config: OpenTelemetryConfigDsl.() -> Unit): Int {
        val api = createOpenTelemetry {
            errorHandler(handler)
            tracerProvider {
                export { FakeSpanProcessor(shutdownCode = { throw IllegalStateException("boom") }) }
            }
            config()
        }.also(sdks::track)
        (api.tracerProvider as TelemetryCloseable).shutdown()
        return handler.userCodeErrors.count { it.severity == SdkErrorSeverity.WARNING }
    }

    private fun fileWithLogLevel(level: SeverityLevel) =
        DeclarativeBehaviorReader { OpenTelemetryBehavior(logLevel = level) }
}
