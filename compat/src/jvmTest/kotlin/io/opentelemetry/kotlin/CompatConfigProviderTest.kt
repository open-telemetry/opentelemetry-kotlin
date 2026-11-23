package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals

internal class CompatConfigProviderTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `createCompatOpenTelemetry configProvider is empty`() {
        val otel = createCompatOpenTelemetry {}.also(sdks::track)
        assertEquals(emptySet(), otel.configProvider.instrumentationConfig.propertyKeys)
    }

    @Test
    fun `toOtelKotlinApi configProvider is empty`() {
        val otel = OtelJavaOpenTelemetry.noop().toOtelKotlinApi().also(sdks::track)
        assertEquals(emptySet(), otel.configProvider.instrumentationConfig.propertyKeys)
    }
}
