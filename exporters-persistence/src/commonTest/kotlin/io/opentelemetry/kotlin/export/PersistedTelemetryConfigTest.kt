package io.opentelemetry.kotlin.export

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PersistedTelemetryConfigTest {

    @Test
    fun testDefaults() {
        val cfg = PersistedTelemetryConfig()
        assertEquals(30, cfg.maxTelemetryAgeInDays)
        assertEquals(100, cfg.maxBatchedItemsPerSignal)
    }
}
