package io.opentelemetry.kotlin.config.envar.reader

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Unset
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class EnvVarReaderTest {

    private val name = "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"

    @Test
    fun `should read an int`() {
        assertEquals(Value(64), EnvVarReader { "64" }.readInt(name))
        assertEquals(Value(0), EnvVarReader { "0" }.readInt(name))
        assertEquals(Value(-1), EnvVarReader { "-1" }.readInt(name))
    }

    @Test
    fun `should read a raw string`() {
        assertEquals(Value("64"), EnvVarReader { "64" }.readString(name))
        assertEquals(Unset, EnvVarReader { "" }.readString(name))
        assertEquals(Unset, EnvVarReader { " " }.readString(name))
        assertEquals(Value("/etc/otel/config.yaml"), EnvVarReader { "/etc/otel/config.yaml" }.readString(name))
    }

    @Test
    fun `should treat an unset env var as unset when read as a string`() {
        assertEquals(Unset, EnvVarReader { null }.readString(name))
    }

    @Test
    fun `should treat a failed read as unset when read as a string`() {
        assertEquals(Unset, EnvVarReader { error("cannot read env vars here") }.readString(name))
    }

    @Test
    fun `should look up the name it was asked for`() {
        var requested: String? = null

        EnvVarReader {
            requested = it
            null
        }.readInt(name)

        assertEquals(name, requested)
    }

    @Test
    fun `should treat an unset env var as unset`() {
        assertEquals(Unset, EnvVarReader { null }.readInt(name))
        assertEquals(Unset, EnvVarReader { null }.readBoolean(name))
    }

    @Test
    fun `should return invalid for a value that is not an int`() {
        listOf("invalid", "64.0", "2147483648").forEach { rawValue ->
            val result = EnvVarReader { rawValue }.readInt(name) as Invalid
            assertEquals(name, result.warning.name)
            assertContains(result.warning.message, rawValue)
        }
    }

    @Test
    fun `should validate a non-negative int`() {
        assertEquals(Value(0), EnvVarReader { "0" }.readNonNegativeInt(name))
        val result = EnvVarReader { "-1" }.readNonNegativeInt(name) as Invalid
        assertContains(result.warning.message, "-1")
    }

    @Test
    fun `should read booleans ignoring case`() {
        mapOf("true" to true, "TRUE" to true, "false" to false, "FALSE" to false)
            .forEach { (rawValue, expected) ->
                assertEquals(Value(expected), EnvVarReader { rawValue }.readBoolean(name))
            }
    }

    @Test
    fun `should return false with a warning for unsupported boolean values`() {
        listOf("1", "yes", "on").forEach { rawValue ->
            val result = EnvVarReader { rawValue }.readBoolean(name) as Value<Boolean>
            val warning = assertNotNull(result.warning)
            assertEquals(false, result.value)
            assertEquals(name, warning.name)
            assertContains(warning.message, rawValue)
            assertContains(warning.message, "false")
        }
    }

    /** The platform supplies the lookup, so a failure to read must not escape into the host app. */
    @Test
    fun `should treat a failed read as unset`() {
        assertEquals(Unset, EnvVarReader { error("cannot read env vars here") }.readInt(name))
    }
}
