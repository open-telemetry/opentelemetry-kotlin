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
        assertEquals(SamplerBehavior.AlwaysOn, toBehavior(env(SamplerEnvVars.ALWAYS_ON)))
    }

    @Test
    fun `should map always_off`() {
        assertEquals(SamplerBehavior.AlwaysOff, toBehavior(env(SamplerEnvVars.ALWAYS_OFF)))
    }

    @Test
    fun `should map parentbased_always_on`() {
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOn),
            toBehavior(env(SamplerEnvVars.PARENT_BASED_ALWAYS_ON)),
        )
    }

    @Test
    fun `should map parentbased_always_off`() {
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff),
            toBehavior(env(SamplerEnvVars.PARENT_BASED_ALWAYS_OFF)),
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
        assertEquals(SamplerEnvVars.SAMPLER, warnings.single().name)
    }

    @Test
    fun `should not warn when sampler is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        SamplerEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun env(sampler: String): (String) -> String? {
        val values = buildMap {
            put(SamplerEnvVars.SAMPLER, sampler)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        SamplerEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
