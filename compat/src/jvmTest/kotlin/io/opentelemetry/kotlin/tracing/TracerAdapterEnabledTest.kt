package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaSpanBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerAdapterEnabledTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `enabled delegates to enabled java tracer`() {
        assertTrue(createTracer { true }.enabled())
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `enabled delegates to disabled java tracer`() {
        assertFalse(createTracer { false }.enabled())
        assertFalse(errorHandler.hasErrors())
    }

    private fun createTracer(isEnabled: () -> Boolean): Tracer =
        TracerAdapter(
            FakeOtelJavaTracer(isEnabled),
            CompatSpanLimitsConfig(),
            CompatContextFactory(),
            errorHandler,
        )

    private class FakeOtelJavaTracer(private val isEnabled: () -> Boolean) : OtelJavaTracer {
        override fun spanBuilder(spanName: String): OtelJavaSpanBuilder =
            throw UnsupportedOperationException()

        override fun isEnabled(): Boolean = isEnabled.invoke()
    }
}
