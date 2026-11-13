package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.TracerProviderConfigDsl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OpenTelemetryConfigFileTest {

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
    fun `a config file with an instrumentation node surfaces it on the configProvider`() {
        val sdk = create { configFile(yamlConfigReader(), writeConfigFile(INSTRUMENTATION_CONFIG_FILE)) }
        val instrumentationConfig = sdk.configProvider.instrumentationConfig
        assertEquals("bar", instrumentationConfig.getString("foo"))
        assertEquals(42L, instrumentationConfig.getStructured("nested")?.getLong("count"))
    }

    @Test
    fun `a config file without an instrumentation node yields an empty configProvider`() {
        val sdk = create { configFile(yamlConfigReader(), writeConfigFile(ATTRIBUTE_LIMIT_CONFIG_FILE)) }
        assertEquals(emptySet(), sdk.configProvider.instrumentationConfig.propertyKeys)
    }

    @Test
    fun `the instrumentation config is read on first access rather than at init`() {
        val file = File(writeConfigFile(ATTRIBUTE_LIMIT_CONFIG_FILE))
        val sdk = create { configFile(yamlConfigReader(), file.absolutePath) }
        file.writeText(INSTRUMENTATION_CONFIG_FILE)
        assertEquals("bar", sdk.configProvider.instrumentationConfig.getString("foo"))
    }

    @Test
    fun `an instrumentation config that cannot be read is reported and empty`() {
        val handler = FakeSdkErrorHandler()
        val file = File(writeConfigFile(INSTRUMENTATION_CONFIG_FILE))
        val sdk = create {
            errorHandler(handler)
            configFile(yamlConfigReader(), file.absolutePath)
        }
        file.delete()
        assertEquals(emptySet(), sdk.configProvider.instrumentationConfig.propertyKeys)
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
        captureStdout { output ->
            val sdk = create { configFile(yamlConfigReader(), writeConfigFile(CONSOLE_CONFIG_FILE)) }
            sdk.tracerProvider.getTracer("test").startSpan("console-span").end()
            sdk.loggerProvider.getLogger("test").emit("console-log")
            assertTrue(awaitOutput(output, "console-span"))
            assertTrue(awaitOutput(output, "console-log"))
        }
    }

    @Test
    fun `a batch config file buffers telemetry until flush`() {
        captureStdout { output ->
            val sdk = create { configFile(yamlConfigReader(), writeConfigFile(BATCH_CONSOLE_CONFIG_FILE)) }
            sdk.tracerProvider.getTracer("test").startSpan("batch-console-span").end()
            sdk.loggerProvider.getLogger("test").emit("batch-console-log")
            assertTrue(output.toString().isEmpty())
            runBlocking { sdk.forceFlush() }
            assertTrue(output.toString().contains("batch-console-span"))
            assertTrue(output.toString().contains("batch-console-log"))
        }
    }

    @Test
    fun `the dsl export takes precedence over console in the config file`() {
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()
        val stdout = captureStdout {
            val sdk = create {
                configFile(yamlConfigReader(), writeConfigFile(CONSOLE_CONFIG_FILE))
                tracerProvider { export { spanProcessor } }
                loggerProvider { export { logProcessor } }
            }
            sdk.tracerProvider.getTracer("test").startSpan("dsl-span").end()
            sdk.loggerProvider.getLogger("test").emit("dsl-log")
        }
        assertEquals(1, spanProcessor.endCalls.size)
        assertEquals(1, logProcessor.logs.size)
        assertFalse(stdout.contains("dsl-span"))
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

    private fun create(config: OpenTelemetryConfigDsl.() -> Unit) = createOpenTelemetry(config = config).also(sdks::track)

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
        sdk.loggerProvider.getLogger("test").emit("log", attributes = {
            repeat(ATTRIBUTE_COUNT) { setStringAttribute("key$it", "value") }
        })
        return spans to logs
    }

    private companion object {
        const val ATTRIBUTE_COUNT = 100
    }
}
