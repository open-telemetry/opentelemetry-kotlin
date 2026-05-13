package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PersistedTelemetryConfigTest {

    @Test
    fun testDefaults() {
        val cfg = PersistedTelemetryConfig()
        assertEquals(30, cfg.maxTelemetryAgeInDays)
        assertEquals(100, cfg.maxBatchedItemsPerSignal)
    }

    @Test
    fun testInvalidConfigDoesNotThrow() {
        val handler = FakeSdkErrorHandler()
        val cfg = PersistedTelemetryConfig(
            desiredMaxBatchedItemsPerSignal = 0,
            desiredMaxTelemetryAgeInDays = 0,
            sdkErrorHandler = handler,
        )
        assertEquals(2, handler.apiMisuses.size)
        val default = PersistedTelemetryConfig()
        assertEquals(default.maxBatchedItemsPerSignal, cfg.maxBatchedItemsPerSignal)
        assertEquals(default.maxTelemetryAgeInDays, cfg.maxTelemetryAgeInDays)
    }
}
