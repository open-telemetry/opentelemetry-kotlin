package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.init.SdkConfigFactory
import io.opentelemetry.kotlin.init.defaultBehaviorReader
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CreateOpenTelemetryConfigFileTest {

    @Test
    fun `a config file that does not exist is reported`() {
        val handler = FakeSdkErrorHandler()
        createOpenTelemetry {
            errorHandler(handler)
            configFile("does-not-exist.yaml")
        }
        assertEquals(1, handler.sdkCodeErrors.size)
    }

    @Test
    fun `a config file that is not valid is reported`() {
        val handler = FakeSdkErrorHandler()
        val path = writeConfigFile("file_format: [not, a, string")
        createOpenTelemetry {
            errorHandler(handler)
            configFile(path)
        }
        assertEquals(1, handler.sdkCodeErrors.size)
    }

    @Test
    fun `a config file supplies the global attribute limits`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONFIG_FILE))
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(64, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun `the dsl takes precedence over the config file`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONFIG_FILE))
            attributeLimits {
                attributeCountLimit = 32
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(32, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(32, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun `a config file with console exporters installs processors`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONSOLE_CONFIG_FILE))
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNotNull(resolver.generateTracingConfig().processor)
        assertNotNull(resolver.generateLoggingConfig().processor)
    }

    @Test
    fun `a batch config file buffers spans until flush`() {
        runBlocking {
            val output = ByteArrayOutputStream()
            val previous = System.out
            System.setOut(PrintStream(output, true))
            try {
                val sdk = createOpenTelemetry {
                    configFile(writeConfigFile(BATCH_CONSOLE_CONFIG_FILE))
                } as OpenTelemetrySdk
                sdk.tracerProvider.getTracer("test").startSpan("batch-console-span").end()
                assertTrue(output.toString().isEmpty())
                sdk.forceFlush()
                assertTrue(output.toString().contains("batch-console-span"))
                sdk.shutdown()
            } finally {
                System.setOut(previous)
            }
        }
    }

    @Test
    fun `the dsl export takes precedence over console in the config file`() {
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONSOLE_CONFIG_FILE))
            tracerProvider { export { spanProcessor } }
            loggerProvider { export { logProcessor } }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertSame(spanProcessor, resolver.generateTracingConfig().processor)
        assertSame(logProcessor, resolver.generateLoggingConfig().processor)
    }

    private fun writeConfigFile(contents: String): String {
        val file = File.createTempFile("opentelemetry-config", ".yaml")
        file.deleteOnExit()
        file.writeText(contents)
        return file.absolutePath
    }

    private companion object {
        val CONFIG_FILE = """
            file_format: "1.0"
            attribute_limits:
              attribute_count_limit: 64
        """.trimIndent()

        val CONSOLE_CONFIG_FILE = """
            file_format: "1.0"
            tracer_provider:
              processors:
                - simple:
                    exporter:
                      console: {}
            logger_provider:
              processors:
                - simple:
                    exporter:
                      console: {}
        """.trimIndent()

        val BATCH_CONSOLE_CONFIG_FILE = """
            file_format: "1.0"
            tracer_provider:
              processors:
                - batch:
                    schedule_delay: 60000
                    exporter:
                      console: {}
        """.trimIndent()
    }
}
