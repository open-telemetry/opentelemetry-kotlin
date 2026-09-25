package io.opentelemetry.kotlin.init

import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class CompatConfigFileTest {

    private val clock = FakeClock()

    @Test
    fun `a config file that does not exist fails initialization`() {
        assertFailsWith<Exception> {
            createCompatOpenTelemetry {
                configFile("does-not-exist.yaml")
            }
        }
    }

    @Test
    fun `a config file that is not valid fails initialization`() {
        val path = writeConfigFile("file_format: [not, a, string")
        assertFailsWith<Exception> {
            createCompatOpenTelemetry {
                configFile(path)
            }
        }
    }

    @Test
    fun `a config file supplies the global attribute limits`() {
        val cfg = CompatOpenTelemetryConfig(clock).apply {
            configFile(writeConfigFile(CONFIG_FILE))
        }
        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val configFactory = CompatSdkConfigFactory(cfg, behavior, clock, CompatContextFactory())
        assertEquals(64, configFactory.spanLimits.attributeCountLimit)
        assertEquals(64, configFactory.logLimits.attributeCountLimit)
    }

    @Test
    fun `the dsl takes precedence over the config file`() {
        val cfg = CompatOpenTelemetryConfig(clock).apply {
            configFile(writeConfigFile(CONFIG_FILE))
            attributeLimits {
                attributeCountLimit = 32
            }
        }
        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val configFactory = CompatSdkConfigFactory(cfg, behavior, clock, CompatContextFactory())
        assertEquals(32, configFactory.spanLimits.attributeCountLimit)
        assertEquals(32, configFactory.logLimits.attributeCountLimit)
    }

    @Test
    fun `a config file with console exporters installs processors`() {
        val stdout = captureStdout {
            val messages = captureJul(LoggingSpanExporter::class.java.name) {
                val sdk = createCompatOpenTelemetry {
                    configFile(writeConfigFile(CONSOLE_CONFIG_FILE))
                }
                sdk.tracerProvider.getTracer("test").startSpan("compat-console-span").end()
                sdk.loggerProvider.getLogger("test").emit(body = "compat-console-log")
            }
            assertTrue(messages.any { it.contains("compat-console-span") })
        }
        assertTrue(stdout.contains("compat-console-log"))
    }

    @Test
    fun `the dsl export takes precedence over console in the config file`() {
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()
        val sdk = createCompatOpenTelemetry {
            configFile(writeConfigFile(CONSOLE_CONFIG_FILE))
            tracerProvider { export { spanProcessor } }
            loggerProvider { export { logProcessor } }
        }
        sdk.tracerProvider.getTracer("test").startSpan("dsl-span").end()
        sdk.loggerProvider.getLogger("test").emit(body = "dsl-log")
        assertEquals(1, spanProcessor.endCalls.size)
        assertEquals(1, logProcessor.logs.size)
    }

    private fun writeConfigFile(contents: String): String {
        val file = File.createTempFile("opentelemetry-config", ".yaml")
        file.deleteOnExit()
        file.writeText(contents)
        return file.absolutePath
    }

    private fun captureJul(loggerName: String, block: () -> Unit): List<String> {
        val logger = Logger.getLogger(loggerName)
        val messages = mutableListOf<String>()
        val handler = object : Handler() {
            override fun publish(record: LogRecord) {
                messages.add(record.message.orEmpty())
            }

            override fun flush() {}

            override fun close() {}
        }
        val previousLevel = logger.level
        val previousUseParent = logger.useParentHandlers
        logger.addHandler(handler)
        logger.level = Level.ALL
        logger.useParentHandlers = false
        try {
            block()
        } finally {
            logger.removeHandler(handler)
            logger.level = previousLevel
            logger.useParentHandlers = previousUseParent
        }
        return messages
    }

    private fun captureStdout(block: () -> Unit): String {
        val buffer = ByteArrayOutputStream()
        val previous = System.out
        System.setOut(PrintStream(buffer, true))
        try {
            block()
        } finally {
            System.setOut(previous)
        }
        return buffer.toString()
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
    }
}
