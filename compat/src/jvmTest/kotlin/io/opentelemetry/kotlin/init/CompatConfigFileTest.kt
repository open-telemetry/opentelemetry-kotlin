package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.factory.CompatContextFactory
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        assertEquals(64, configFactory.attributeLimits.attributeCountLimit)
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
        assertEquals(32, configFactory.attributeLimits.attributeCountLimit)
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
    }
}
