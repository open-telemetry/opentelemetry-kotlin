package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenTelemetryEnvVarsTest {

    private fun behaviorFrom(vars: Map<String, String>) =
        OpenTelemetryEnvVars(EnvVarReader { vars[it] }).toBehavior()

    @Test
    fun emptyEnv() {
        val behavior = behaviorFrom(emptyMap())
        assertEquals(AttributeLimitsBehavior(), behavior.attributeLimits)
        assertEquals(LogLimitsBehavior(), behavior.loggerProvider?.logLimits)
    }

    @Test
    fun globalAndLogRecordLimits() {
        val behavior = behaviorFrom(
            mapOf(
                "OTEL_ATTRIBUTE_COUNT_LIMIT" to "64",
                "OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "256",
                "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT" to "8",
            )
        )
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            behavior.attributeLimits,
        )
        assertEquals(8, behavior.loggerProvider?.logLimits?.attributeCountLimit)
    }

    @Test
    fun doesNotAggregateSpanLimits() {
        assertNull(behaviorFrom(mapOf("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT" to "4")).tracerProvider)
    }

    @Test
    fun disallowedValueUnset() {
        val behavior = behaviorFrom(mapOf("OTEL_ATTRIBUTE_COUNT_LIMIT" to "-1"))
        assertNull(behavior.attributeLimits?.attributeCountLimit)
    }
}
