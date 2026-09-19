package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.init.SdkConfigFactory
import io.opentelemetry.kotlin.init.defaultBehaviorReader
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class CreateOpenTelemetryConfigFileTest {

    @Test
    fun `a config file that does not exist fails initialization`() {
        assertFailsWith<Exception> {
            createOpenTelemetry {
                configFile("does-not-exist.yaml")
            }
        }
    }

    @Test
    fun `a config file that is not valid fails initialization`() {
        val path = writeConfigFile("file_format: [not, a, string")
        assertFailsWith<Exception> {
            createOpenTelemetry {
                configFile(path)
            }
        }
    }

    @Test
    fun `a config file supplies the global attribute limits`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONFIG_FILE))
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
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
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(32, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(32, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun `a config file with console exporters installs processors`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONSOLE_CONFIG_FILE))
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNotNull(resolver.generateTracingConfig().processor)
        assertNotNull(resolver.generateLoggingConfig().processor)
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
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
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
    }
}
