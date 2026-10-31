@file:OptIn(ExperimentalForeignApi::class)

package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.setenv
import platform.posix.unsetenv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class PlatformEnvVarReaderTest {

    @Test
    fun `should read envars on Linux`() {
        val name = "OTEL_KOTLIN_CONFIG_TEST_ENV_VAR"
        assertEquals(0, setenv(name, "42", 1))
        try {
            val reader = assertNotNull(platformEnvVarReader())
            assertEquals(Value("42"), reader.readString(name))
        } finally {
            unsetenv(name)
        }
    }
}
