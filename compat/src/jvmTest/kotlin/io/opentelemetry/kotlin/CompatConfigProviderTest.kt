package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import org.junit.Test
import kotlin.test.assertEquals

internal class CompatConfigProviderTest {

    @Test
    fun `createCompatOpenTelemetry configProvider is empty`() {
        val otel = createCompatOpenTelemetry {}
        assertEquals(emptySet(), otel.configProvider.instrumentationConfig.propertyKeys)
    }

    @Test
    fun `toOtelKotlinApi configProvider is empty`() {
        val otel = OtelJavaOpenTelemetry.noop().toOtelKotlinApi()
        assertEquals(emptySet(), otel.configProvider.instrumentationConfig.propertyKeys)
    }
}
