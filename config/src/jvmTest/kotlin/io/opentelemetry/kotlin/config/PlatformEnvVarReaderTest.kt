package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class PlatformEnvVarReaderTest {

    @Test
    fun `should read envars on the JVM`() {
        val reader = assertNotNull(platformEnvVarReader())
        val expected = assertNotNull(System.getenv("PATH"))
        assertEquals(Value(expected), reader.readString("PATH"))
    }
}
