package io.opentelemetry.kotlin.config.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ConfigResolverImplTest {

    private val resolver = ConfigResolverImpl()

    @Test
    fun leavesEverythingUnsetWhenNoLayerConfiguresAnything() {
        val resolved = resolver.resolve(envars = null, declarativeFile = null, dsl = null)

        assertEquals(OpenTelemetryConfigModel(), resolved)
        assertNull(resolved.tracerProvider)
    }

    @Test
    fun leavesUnmentionedLimitsUnset() {
        val limits = resolveSpanLimits(dsl = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 5)))

        assertEquals(5, limits?.linkCountLimit)
        assertNull(limits?.eventCountLimit)
    }

    @Test
    fun eachLayerAloneIsApplied() {
        val limits = SpanLimitsConfigModel(linkCountLimit = 5)

        assertEquals(limits, resolveSpanLimits(envars = configWithSpanLimits(limits)))
        assertEquals(limits, resolveSpanLimits(declarativeFile = configWithSpanLimits(limits)))
        assertEquals(limits, resolveSpanLimits(dsl = configWithSpanLimits(limits)))
    }

    @Test
    fun dslOverridesEnvars() {
        val limits = resolveSpanLimits(
            envars = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 5, eventCountLimit = 6)),
            dsl = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 50)),
        )

        assertEquals(50, limits?.linkCountLimit)
        assertEquals(6, limits?.eventCountLimit)
    }

    @Test
    fun dslOverridesDeclarativeFile() {
        val limits = resolveSpanLimits(
            declarativeFile = configWithSpanLimits(
                SpanLimitsConfigModel(linkCountLimit = 5, eventCountLimit = 6),
            ),
            dsl = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 50)),
        )

        assertEquals(50, limits?.linkCountLimit)
        assertEquals(6, limits?.eventCountLimit)
    }

    /**
     * The envars are dropped wholesale rather than merged beneath the file, so a field the file
     * leaves unset stays unset and does not fall through to the envar value.
     */
    @Test
    fun declarativeFileReplacesEnvarsRatherThanMergingWithThem() {
        val limits = resolveSpanLimits(
            envars = configWithSpanLimits(
                SpanLimitsConfigModel(linkCountLimit = 5, eventCountLimit = 6),
            ),
            declarativeFile = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 50)),
        )

        assertEquals(50, limits?.linkCountLimit)
        assertNull(limits?.eventCountLimit)
    }

    @Test
    fun emptyDeclarativeFileStillReplacesEnvars() {
        val resolved = resolver.resolve(
            envars = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 5)),
            declarativeFile = OpenTelemetryConfigModel(),
            dsl = null,
        )

        assertEquals(OpenTelemetryConfigModel(), resolved)
    }

    @Test
    fun dslWinsOverBothLowerLayers() {
        val limits = resolveSpanLimits(
            envars = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 5)),
            declarativeFile = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 50)),
            dsl = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 500)),
        )

        assertEquals(500, limits?.linkCountLimit)
    }

    @Test
    fun preservesAValueOfZero() {
        val limits = resolveSpanLimits(dsl = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 0)))

        assertEquals(0, limits?.linkCountLimit)
    }

    private fun resolveSpanLimits(
        envars: OpenTelemetryConfigModel? = null,
        declarativeFile: OpenTelemetryConfigModel? = null,
        dsl: OpenTelemetryConfigModel? = null,
    ) = resolver.resolve(envars, declarativeFile, dsl).tracerProvider?.spanLimits

    private fun configWithSpanLimits(spanLimits: SpanLimitsConfigModel) =
        OpenTelemetryConfigModel(tracerProvider = TracerProviderConfigModel(spanLimits = spanLimits))
}
