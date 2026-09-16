package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GlobalAttributeLimitsTest {

    private val resolver = BehaviorResolverImpl()

    @Test
    fun globalLimitsReachBothSignalsWhenNeitherIsConfigured() {
        val resolved = resolve(AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 8))
        assertEquals(64, resolved.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(8, resolved.tracerProvider?.spanLimits?.attributeValueLengthLimit)
        assertEquals(64, resolved.loggerProvider?.logLimits?.attributeCountLimit)
        assertEquals(8, resolved.loggerProvider?.logLimits?.attributeValueLengthLimit)
    }

    @Test
    fun signalLimitWinsOverGlobal() {
        val resolved = resolver.resolve(
            envars = null,
            declarativeFile = null,
            dsl = OpenTelemetryBehavior(
                attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 64),
                tracerProvider = TracerProviderBehavior(
                    spanLimits = SpanLimitsBehavior(attributeCountLimit = 32),
                ),
                loggerProvider = LoggerProviderBehavior(
                    logLimits = LogLimitsBehavior(attributeCountLimit = 16),
                ),
            ),
        )
        assertEquals(32, resolved.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(16, resolved.loggerProvider?.logLimits?.attributeCountLimit)
    }

    @Test
    fun leavesNonAttributeSpanLimitsAlone() {
        val resolved = resolver.resolve(
            envars = null,
            declarativeFile = null,
            dsl = OpenTelemetryBehavior(
                attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 64),
                tracerProvider = TracerProviderBehavior(
                    spanLimits = SpanLimitsBehavior(linkCountLimit = 5),
                ),
            ),
        )
        val spanLimits = resolved.tracerProvider?.spanLimits
        assertEquals(5, spanLimits?.linkCountLimit)
        assertEquals(64, spanLimits?.attributeCountLimit)
        assertNull(spanLimits?.eventCountLimit)
        assertNull(spanLimits?.attributeCountPerEventLimit)
    }

    @Test
    fun theWinningGlobalBlockIsTheOneThatApplies() {
        val resolved = resolver.resolve(
            envars = OpenTelemetryBehavior(
                attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 5),
            ),
            declarativeFile = null,
            dsl = OpenTelemetryBehavior(
                attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 50),
            ),
        )
        assertEquals(50, resolved.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(50, resolved.loggerProvider?.logLimits?.attributeCountLimit)
    }

    @Test
    fun preservesAValueOfZero() {
        val resolved = resolve(AttributeLimitsBehavior(attributeCountLimit = 0))
        assertEquals(0, resolved.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(0, resolved.loggerProvider?.logLimits?.attributeCountLimit)
    }

    private fun resolve(attributeLimits: AttributeLimitsBehavior) = resolver.resolve(
        envars = null,
        declarativeFile = null,
        dsl = OpenTelemetryBehavior(attributeLimits = attributeLimits),
    )
}
