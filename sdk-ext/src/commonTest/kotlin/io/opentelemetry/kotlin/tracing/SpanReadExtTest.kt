package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanReadExtTest {

    @Test
    fun spanWithoutReadableInterfaceReturnsNoopReadableSpan() {
        assertSame(NoopReadableSpan, FakeSpan().toReadableSpan())
    }

    @Test
    fun nonRecordingSpanReturnsNoopReadableSpan() {
        val span = FakeSpan().apply { end() }
        assertFalse(span.isRecording())

        val readable = span.toReadableSpan()
        assertSame(NoopReadableSpan, readable)
        assertFalse(readable.spanContext.isValid)
        assertSame(readable, readable.toSpanData())
    }

    @Test
    fun recordingReadableSpanIsReturnedUnchanged() {
        val data = FakeSpanData()
        val span = object : Span by FakeSpan(), ReadableSpan, SpanData by data {
            override val parent: SpanContext = data.parent
            override val spanContext: SpanContext = data.spanContext
            override fun toSpanData(): SpanData = data
        }

        assertTrue(span.isRecording())
        assertSame(span, span.toReadableSpan())
    }
}
