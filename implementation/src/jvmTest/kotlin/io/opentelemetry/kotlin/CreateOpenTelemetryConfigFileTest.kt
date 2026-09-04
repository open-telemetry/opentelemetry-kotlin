package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        assertEquals(64, cfg.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, cfg.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun `the dsl takes precedence over the config file`() {
        val cfg = OpenTelemetryConfigImpl(FakeClock()).apply {
            configFile(writeConfigFile(CONFIG_FILE))
            attributeLimits {
                attributeCountLimit = 32
            }
        }
        assertEquals(32, cfg.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(32, cfg.generateLoggingConfig().logLimits.attributeCountLimit)
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
