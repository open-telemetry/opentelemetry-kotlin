package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.CompatContextFactory
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtelJavaSpanBuilderAdapterTimestampTest {

    private val tracer = CapturingTracer()

    @Test
    fun `negative start timestamp is ignored`() {
        OtelJavaSpanBuilderAdapter(tracer, "span", CompatContextFactory())
            .setStartTimestamp(42, TimeUnit.NANOSECONDS)
            .setStartTimestamp(-1, TimeUnit.NANOSECONDS)
            .startSpan()
        assertEquals(42, tracer.startTimestamp)
    }

    @Test
    fun `zero start timestamp is ignored`() {
        OtelJavaSpanBuilderAdapter(tracer, "span", CompatContextFactory())
            .setStartTimestamp(42, TimeUnit.NANOSECONDS)
            .setStartTimestamp(0, TimeUnit.NANOSECONDS)
            .startSpan()
        assertEquals(42, tracer.startTimestamp)
    }

    private class CapturingTracer : Tracer {
        var startTimestamp: Long? = null

        override fun enabled(): Boolean = true

        override fun startSpan(
            name: String,
            parentContext: Context?,
            spanKind: SpanKind,
            startTimestamp: Long?,
            action: (SpanCreationAction.() -> Unit)?
        ): Span {
            this.startTimestamp = startTimestamp
            return FakeSpan()
        }
    }
}
