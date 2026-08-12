package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.EnvVarConstants
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanLimitEnvVarConfigProcessorImplTest {

    @Test
    fun `should configure all span limit environment variables`() {
        val processor = SpanLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.SpanLimits.envVars
        )
        val defaultValue = OpenTelemetryConfigImpl(FakeClock()).generateTracingConfig().spanLimits

        val config = processor.configure(defaultValue = defaultValue) {
            getFakeEnvVarValue(it)
        }

        assertEquals(1, config.attributeCountLimit)
        assertEquals(2, config.attributeValueLengthLimit)
        assertEquals(3, config.eventCountLimit)
        assertEquals(4, config.linkCountLimit)
        assertEquals(5, config.attributeCountPerEventLimit)
        assertEquals(6, config.attributeCountPerLinkLimit)
    }

    @Test
    fun `should preserve defaults when environment variables are unavailable`() {
        val processor = SpanLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.SpanLimits.envVars
        )
        val defaultValue = OpenTelemetryConfigImpl(FakeClock()).generateTracingConfig().spanLimits

        val config = processor.configure(defaultValue = defaultValue) { null }

        assertEquals(128, config.attributeCountLimit)
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.eventCountLimit)
        assertEquals(128, config.linkCountLimit)
        assertEquals(128, config.attributeCountPerEventLimit)
        assertEquals(128, config.attributeCountPerLinkLimit)
    }

    @Test
    fun `should preserve defaults for invalid environment variables`() {
        val processor = SpanLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.SpanLimits.envVars
        )
        val defaultValue = OpenTelemetryConfigImpl(FakeClock()).generateTracingConfig().spanLimits

        val config = processor.configure(defaultValue = defaultValue) {
            getInvalidEnvVarValue(it)
        }

        assertEquals(128, config.attributeCountLimit)
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.eventCountLimit)
        assertEquals(128, config.linkCountLimit)
        assertEquals(128, config.attributeCountPerEventLimit)
        assertEquals(128, config.attributeCountPerLinkLimit)
    }

    @Test
    fun `should preserve configured values for unset environment variables`() {
        val processor = SpanLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.SpanLimits.envVars
        )
        val otelConfig = OpenTelemetryConfigImpl(FakeClock()).apply {
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 10
                    attributeValueLengthLimit = 20
                    eventCountLimit = 30
                    linkCountLimit = 40
                    attributeCountPerEventLimit = 50
                    attributeCountPerLinkLimit = 60
                }
            }
        }
        val defaultValue = otelConfig.generateTracingConfig().spanLimits

        val config = processor.configure(defaultValue = defaultValue) {
            if (it == "OTEL_SPAN_EVENT_COUNT_LIMIT") {
                "3"
            } else {
                null
            }
        }

        assertEquals(10, config.attributeCountLimit)
        assertEquals(20, config.attributeValueLengthLimit)
        assertEquals(3, config.eventCountLimit)
        assertEquals(40, config.linkCountLimit)
        assertEquals(50, config.attributeCountPerEventLimit)
        assertEquals(60, config.attributeCountPerLinkLimit)
    }

    private fun getFakeEnvVarValue(envVar: String): String {
        return when (envVar) {
            "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT" -> "1"
            "OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT" -> "2"
            "OTEL_SPAN_EVENT_COUNT_LIMIT" -> "3"
            "OTEL_SPAN_LINK_COUNT_LIMIT" -> "4"
            "OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT" -> "5"
            "OTEL_LINK_ATTRIBUTE_COUNT_LIMIT" -> "6"
            else -> "-1"
        }
    }

    private fun getInvalidEnvVarValue(envVar: String): String {
        return when (envVar) {
            "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT" -> "-1"
            "OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT" -> ""
            "OTEL_SPAN_EVENT_COUNT_LIMIT" -> "invalid"
            "OTEL_SPAN_LINK_COUNT_LIMIT" -> "2147483648"
            "OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT" -> "-5"
            "OTEL_LINK_ATTRIBUTE_COUNT_LIMIT" -> "1.5"
            else -> "0"
        }
    }
}
