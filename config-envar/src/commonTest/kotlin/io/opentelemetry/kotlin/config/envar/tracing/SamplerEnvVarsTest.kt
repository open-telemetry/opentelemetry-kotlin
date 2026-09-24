package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SamplerEnvVarsTest {

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map always_on`() {
        assertEquals(SamplerBehavior.AlwaysOn, toBehavior(env("always_on")))
    }

    @Test
    fun `should map always_off`() {
        assertEquals(SamplerBehavior.AlwaysOff, toBehavior(env("always_off")))
    }

    @Test
    fun `should map parentbased_always_on`() {
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOn),
            toBehavior(env("parentbased_always_on")),
        )
    }

    @Test
    fun `should map parentbased_always_off`() {
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff),
            toBehavior(env("parentbased_always_off")),
        )
    }

    @Test
    fun `should leave unknown sampler unset`() {
        listOf("sampler_test", "").forEach { name ->
            assertNull(toBehavior(env(name)), "<$name> should not configure a sampler")
        }
    }

    @Test
    fun `should warn on unknown sampler`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        SamplerEnvVars(reportingEnvVarReader(env("not_a_sampler"), warnings::add)).toBehavior()
        assertEquals(1, warnings.size)
        assertEquals("OTEL_TRACES_SAMPLER", warnings.single().name)
    }

    @Test
    fun `should not warn when sampler is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        SamplerEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun env(sampler: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_SAMPLER", sampler)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        SamplerEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
