package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PlatformEnvVarReaderTest {

    @Test
    fun `should read envars on Node`() {
        val env = js("process.env")
        env["OTEL_KOTLIN_CONFIG_TEST_ENV_VAR"] = "42"
        try {
            val reader = assertNotNull(platformEnvVarReader())
            assertEquals(Value("42"), reader.readString("OTEL_KOTLIN_CONFIG_TEST_ENV_VAR"))
        } finally {
            js("delete process.env.OTEL_KOTLIN_CONFIG_TEST_ENV_VAR")
        }
    }

    @Test
    fun `should not read envars in a browser`() {
        // tests run on Node, so hide `process` to look like a browser
        val global: dynamic = js("globalThis")
        val process = global.process
        global.process = undefined
        try {
            assertNull(platformEnvVarReader())
        } finally {
            global.process = process
        }
    }
}
