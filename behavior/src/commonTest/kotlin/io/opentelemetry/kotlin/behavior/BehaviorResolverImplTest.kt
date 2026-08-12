package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BehaviorResolverImplTest {

    private val resolver = BehaviorResolverImpl()

    @Test
    fun leavesEverythingUnsetWhenNoLayerConfiguresAnything() {
        val resolved = resolver.resolve(envars = null, declarativeFile = null, dsl = null)

        assertEquals(OpenTelemetryBehavior(), resolved)
        assertNull(resolved.tracerProvider)
    }

    @Test
    fun leavesUnmentionedLimitsUnset() {
        val limits = resolveSpanLimits(dsl = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 5)))

        assertEquals(5, limits?.linkCountLimit)
        assertNull(limits?.eventCountLimit)
    }

    @Test
    fun eachLayerAloneIsApplied() {
        val limits = SpanLimitsBehavior(linkCountLimit = 5)

        assertEquals(limits, resolveSpanLimits(envars = configWithSpanLimits(limits)))
        assertEquals(limits, resolveSpanLimits(declarativeFile = configWithSpanLimits(limits)))
        assertEquals(limits, resolveSpanLimits(dsl = configWithSpanLimits(limits)))
    }

    @Test
    fun dslOverridesEnvars() {
        val limits = resolveSpanLimits(
            envars = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 5, eventCountLimit = 6)),
            dsl = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 50)),
        )

        assertEquals(50, limits?.linkCountLimit)
        assertEquals(6, limits?.eventCountLimit)
    }

    @Test
    fun dslOverridesDeclarativeFile() {
        val limits = resolveSpanLimits(
            declarativeFile = configWithSpanLimits(
                SpanLimitsBehavior(linkCountLimit = 5, eventCountLimit = 6),
            ),
            dsl = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 50)),
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
                SpanLimitsBehavior(linkCountLimit = 5, eventCountLimit = 6),
            ),
            declarativeFile = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 50)),
        )

        assertEquals(50, limits?.linkCountLimit)
        assertNull(limits?.eventCountLimit)
    }

    @Test
    fun emptyDeclarativeFileStillReplacesEnvars() {
        val resolved = resolver.resolve(
            envars = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 5)),
            declarativeFile = OpenTelemetryBehavior(),
            dsl = null,
        )

        assertEquals(OpenTelemetryBehavior(), resolved)
    }

    @Test
    fun dslWinsOverBothLowerLayers() {
        val limits = resolveSpanLimits(
            envars = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 5)),
            declarativeFile = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 50)),
            dsl = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 500)),
        )

        assertEquals(500, limits?.linkCountLimit)
    }

    @Test
    fun preservesAValueOfZero() {
        val limits = resolveSpanLimits(dsl = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 0)))

        assertEquals(0, limits?.linkCountLimit)
    }

    private fun resolveSpanLimits(
        envars: OpenTelemetryBehavior? = null,
        declarativeFile: OpenTelemetryBehavior? = null,
        dsl: OpenTelemetryBehavior? = null,
    ) = resolver.resolve(envars, declarativeFile, dsl).tracerProvider?.spanLimits

    private fun configWithSpanLimits(spanLimits: SpanLimitsBehavior) =
        OpenTelemetryBehavior(tracerProvider = TracerProviderBehavior(spanLimits = spanLimits))
}
