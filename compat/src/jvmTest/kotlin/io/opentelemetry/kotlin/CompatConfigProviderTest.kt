package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalApi::class)
internal class CompatConfigProviderTest {

    @Test
    fun `createCompatOpenTelemetry configProvider is non-null and empty`() {
        val otel = createCompatOpenTelemetry {}
        val instrumentationConfig = assertNotNull(otel.configProvider.instrumentationConfig)
        assertEquals(emptySet(), instrumentationConfig.propertyKeys)
    }

    @Test
    fun `toOtelKotlinApi configProvider is non-null and empty`() {
        val otel = OtelJavaOpenTelemetry.noop().toOtelKotlinApi()
        val instrumentationConfig = assertNotNull(otel.configProvider.instrumentationConfig)
        assertEquals(emptySet(), instrumentationConfig.propertyKeys)
    }
}
