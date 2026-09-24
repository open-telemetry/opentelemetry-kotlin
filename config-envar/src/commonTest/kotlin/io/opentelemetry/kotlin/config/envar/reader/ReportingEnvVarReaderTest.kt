package io.opentelemetry.kotlin.config.envar.reader

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ReportingEnvVarReaderTest {

    private val name = "OTEL_TEST"

    @Test
    fun `should return parsed values and apply unset defaults`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        assertEquals("value", reader("value").readString(name))
        assertTrue(reader("true").readBoolean(name))
        assertNull(reader(null, warnings::add).readString(name))
        assertFalse(reader(null, warnings::add).readBoolean(name))
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `should report invalid values and apply type defaults`() {
        val warnings = mutableListOf<EnvVarReadWarning>()

        assertFalse(reader("invalid", warnings::add).readBoolean(name))
        assertNull(reader("invalid", warnings::add).readNonNegativeInt(name))
        assertNull(reader("-1", warnings::add).readNonNegativeInt(name))

        assertEquals(3, warnings.size)
        assertTrue(warnings.all { it.name == name })
        assertContains(warnings.first().message, "false")
    }

    @Test
    fun `should resolve parsed results`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        assertEquals(5, reader("known").readStringAndTransform(name) { Value(it.length) })
        assertNull(
            reader("unknown", warnings::add).readStringAndTransform<Int>(
                name,
            ) {
                Invalid(EnvVarReadWarning(name, "Unknown value '$it'; ignoring"))
            }
        )
        assertEquals(name, warnings.single().name)
    }

    private fun reader(
        rawValue: String?,
        onWarning: (EnvVarReadWarning) -> Unit = {},
    ): ReportingEnvVarReader = ReportingEnvVarReader(EnvVarReader { rawValue }, onWarning)
}
