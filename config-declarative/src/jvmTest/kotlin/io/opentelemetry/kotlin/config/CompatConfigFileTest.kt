package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.TracerProviderConfigDsl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompatConfigFileTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `a config file that does not exist is reported`() {
        val handler = FakeSdkErrorHandler()
        create {
            errorHandler(handler)
            configFile(yamlConfigReader(), "does-not-exist.yaml")
        }
        assertEquals(1, handler.errors.size)
        assertEquals("OTEL_CONFIG_FILE", handler.apiMisuses.single().api)
    }

    @Test
    fun `a config file that is not valid is reported`() {
        val handler = FakeSdkErrorHandler()
        create {
            errorHandler(handler)
            configFile(yamlConfigReader(), writeConfigFile("file_format: [not, a, string"))
        }
        assertEquals(1, handler.errors.size)
        assertEquals("OTEL_CONFIG_FILE", handler.apiMisuses.single().api)
    }

    @Test
    fun `a config file supplies the global attribute limits`() {
        val (spans, logs) = emitWithAttributes(ATTRIBUTE_LIMIT_CONFIG_FILE)
        assertEquals(64, spans.endCalls.single().attributes.size)
        assertEquals(64, logs.logs.single().attributes.size)
    }

    @Test
    fun `the dsl takes precedence over the config file`() {
        val (spans, logs) = emitWithAttributes(ATTRIBUTE_LIMIT_CONFIG_FILE) {
            attributeLimits { attributeCountLimit = 32 }
        }
        assertEquals(32, spans.endCalls.single().attributes.size)
        assertEquals(32, logs.logs.single().attributes.size)
    }

    @Test
    fun `a config file with console exporters installs processors`() {
        val stdout = captureStdout {
            val messages = captureJul(LOGGING_SPAN_EXPORTER) {
                val sdk = create { configFile(yamlConfigReader(), writeConfigFile(CONSOLE_CONFIG_FILE)) }
                sdk.tracerProvider.getTracer("test").startSpan("compat-console-span").end()
                sdk.loggerProvider.getLogger("test").emit(body = "compat-console-log")
            }
            assertTrue(messages.any { it.contains("compat-console-span") })
        }
        assertTrue(stdout.contains("compat-console-log"))
    }

    @Test
    fun `a batch config file buffers spans until flush`() {
        captureJul(LOGGING_SPAN_EXPORTER) { messages ->
            val sdk = create { configFile(yamlConfigReader(), writeConfigFile(BATCH_CONSOLE_CONFIG_FILE)) }
            sdk.tracerProvider.getTracer("test").startSpan("batch-console-span").end()
            assertTrue(messages.isEmpty())
            runBlocking { sdk.forceFlush() }
            assertTrue(messages.any { it.contains("batch-console-span") })
        }
    }

    @Test
    fun `a batch config file buffers log records until flush`() {
        captureStdout { buffer ->
            val sdk = create { configFile(yamlConfigReader(), writeConfigFile(BATCH_CONSOLE_CONFIG_FILE)) }
            sdk.loggerProvider.getLogger("test").emit(body = "compat-console-log")
            assertTrue(buffer.toString().isEmpty())
            runBlocking { sdk.forceFlush() }
            assertTrue(buffer.toString().contains("compat-console-log"))
        }
    }

    @Test
    fun `the dsl export takes precedence over console in the config file`() {
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()
        val sdk = create {
            configFile(yamlConfigReader(), writeConfigFile(CONSOLE_CONFIG_FILE))
            tracerProvider { export { spanProcessor } }
            loggerProvider { export { logProcessor } }
        }
        sdk.tracerProvider.getTracer("test").startSpan("dsl-span").end()
        sdk.loggerProvider.getLogger("test").emit(body = "dsl-log")
        assertEquals(1, spanProcessor.endCalls.size)
        assertEquals(1, logProcessor.logs.size)
    }

    @Test
    fun `a config file sampler is applied when the dsl omits a sampler`() {
        assertFalse(isRecording())
    }

    @Test
    fun `the dsl sampler takes precedence over the config file`() {
        assertTrue(isRecording { sampler { alwaysOn() } })
    }

    private fun isRecording(tracing: TracerProviderConfigDsl.() -> Unit = {}): Boolean {
        val sdk = create {
            configFile(yamlConfigReader(), writeConfigFile(ALWAYS_OFF_SAMPLER_CONFIG_FILE))
            tracerProvider {
                export { FakeSpanProcessor() }
                tracing()
            }
        }
        return sdk.tracerProvider.getTracer("test").startSpan("span").isRecording()
    }

    private fun create(config: OpenTelemetryConfigDsl.() -> Unit) =
        createCompatOpenTelemetry(config = config).also(sdks::track)

    private fun emitWithAttributes(
        configFile: String,
        config: OpenTelemetryConfigDsl.() -> Unit = {},
    ): Pair<FakeSpanProcessor, FakeLogRecordProcessor> {
        val spans = FakeSpanProcessor()
        val logs = FakeLogRecordProcessor()
        val sdk = create {
            configFile(yamlConfigReader(), writeConfigFile(configFile))
            tracerProvider { export { spans } }
            loggerProvider { export { logs } }
            config()
        }
        sdk.tracerProvider.getTracer("test").startSpan("span") {
            repeat(ATTRIBUTE_COUNT) { setStringAttribute("key$it", "value") }
        }.end()
        sdk.loggerProvider.getLogger("test").emit(body = "log", attributes = {
            repeat(ATTRIBUTE_COUNT) { setStringAttribute("key$it", "value") }
        })
        return spans to logs
    }

    private companion object {
        const val ATTRIBUTE_COUNT = 100
        const val LOGGING_SPAN_EXPORTER = "io.opentelemetry.exporter.logging.LoggingSpanExporter"
    }
}
