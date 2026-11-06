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
    fun `should map traceidratio with arg`() {
        assertEquals(
            SamplerBehavior.Probability(0.25),
            toBehavior(env(SamplerEnvVars.TRACE_ID_RATIO, arg = "0.25")),
        )
    }

    @Test
    fun `should map parentbased_traceidratio with arg`() {
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.Probability(0.25)),
            toBehavior(env(SamplerEnvVars.PARENT_BASED_TRACE_ID_RATIO, arg = "0.25")),
        )
    }

    @Test
    fun `should default traceidratio arg to 1_0`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        val behavior = SamplerEnvVars(reportingEnvVarReader(env(SamplerEnvVars.TRACE_ID_RATIO), warnings::add))
            .toBehavior()
        assertEquals(SamplerBehavior.Probability(1.0), behavior)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should warn and default on invalid traceidratio arg`() {
        listOf("abc", "-0.1", "1.5", "NaN").forEach { arg ->
            val warnings = mutableListOf<EnvVarReadWarning>()
            val behavior = SamplerEnvVars(
                reportingEnvVarReader(env(SamplerEnvVars.TRACE_ID_RATIO, arg = arg), warnings::add)
            ).toBehavior()
            assertEquals(SamplerBehavior.Probability(1.0), behavior, "<$arg> should fall back to 1.0")
            assertEquals(SamplerEnvVars.SAMPLER_ARG, warnings.single().name, "<$arg> should warn")
        }
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

    private fun env(sampler: String, arg: String? = null): (String) -> String? {
        val values = buildMap {
            put(SamplerEnvVars.SAMPLER, sampler)
            arg?.let { put(SamplerEnvVars.SAMPLER_ARG, it) }
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        SamplerEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
