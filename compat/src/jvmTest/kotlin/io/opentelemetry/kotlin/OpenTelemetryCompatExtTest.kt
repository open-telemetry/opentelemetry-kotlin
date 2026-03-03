package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import org.junit.Test
import kotlin.test.assertSame

internal class OpenTelemetryCompatExtTest {

    @Test
    fun `toOtelJavaApi returns noop when called on noop instance`() {
        val noopKotlinInstance = NoopOpenTelemetry
        val result = noopKotlinInstance.toOtelJavaApi()
        assertSame(OtelJavaOpenTelemetry.noop(), result)
    }
}
