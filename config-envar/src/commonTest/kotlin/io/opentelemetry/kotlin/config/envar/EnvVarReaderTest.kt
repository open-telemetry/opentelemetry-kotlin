package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class EnvVarReaderTest {

    private val name = envVarName("OTEL_ATTRIBUTE_COUNT_LIMIT")

    @Test
    fun `should read an int`() {
        assertEquals(64, EnvVarReader { "64" }.readInt(name))
        assertEquals(0, EnvVarReader { "0" }.readInt(name))
        assertEquals(-1, EnvVarReader { "-1" }.readInt(name))
    }

    @Test
    fun `should look up the name it was asked for`() {
        var requested: String? = null

        EnvVarReader {
            requested = it
            null
        }.readInt(name)
        assertEquals(name.value, requested)
    }

    @Test
    fun `should treat an unset env var as unset`() {
        assertNull(EnvVarReader { null }.readInt(name))
    }

    @Test
    fun `should treat a value that is not an int as unset`() {
        listOf("invalid", "", " ", "64.0", "2147483648").forEach { rawValue ->
            assertNull(EnvVarReader { rawValue }.readInt(name), "<$rawValue> should not be read")
        }
    }

    /**
     * The platform supplies the lookup, so a failure to read must not escape into the host app.
     */
    @Test
    fun `should treat a failed read as unset`() {
        assertNull(EnvVarReader { error("cannot read env vars here") }.readInt(name))
    }
}
