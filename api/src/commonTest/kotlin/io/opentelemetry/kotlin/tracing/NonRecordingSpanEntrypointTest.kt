package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AnyValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class NonRecordingSpanEntrypointTest {

    private val spanContext = createSpanContext(
        traceId = "0af7651916cd43dd8448eb211c80319c",
        spanId = "b7ad6b7169203331",
    ) {
        isSampled = true
        isRemote = true
    }

    @Test
    fun testWrapsSpanContext() {
        val span = createNonRecordingSpan(spanContext)
        assertSame(spanContext, span.spanContext)
        assertSame(createInvalidSpanContext(), span.parent)
        assertFalse(span.isRecording())
    }

    @Test
    fun testWrapsSpanContextWithParent() {
        val parent = createSpanContext(
            traceId = "0af7651916cd43dd8448eb211c80319c",
            spanId = "00f067aa0ba902b7",
        )
        val span = createNonRecordingSpan(spanContext, parent = parent)
        assertSame(spanContext, span.spanContext)
        assertSame(parent, span.parent)
        assertFalse(span.isRecording())
    }

    @Test
    fun testMutatorsAreNoops() {
        val span = createNonRecordingSpan(spanContext)
        span.setName("name")
        span.setStatus(StatusData.Error("error"))
        span.setStringAttribute("string", "value")
        span.setBooleanAttribute("bool", true)
        span.setLongAttribute("long", 5L)
        span.setDoubleAttribute("double", 3.4)
        span.setStringListAttribute("list", listOf("a", "b"))
        span.setBooleanListAttribute("boolList", listOf(true, false))
        span.setLongListAttribute("longList", listOf(1L, 2L))
        span.setDoubleListAttribute("doubleList", listOf(1.1, 2.2))
        span.setByteArrayAttribute("bytes", byteArrayOf(1, 2))
        span.setAnyValueAttribute("any", AnyValue.StringValue("value"))
        span.addEvent("test")
        span.addLink(createInvalidSpanContext())
        span.end()
        span.end(5)

        assertFalse(span.isRecording())
        assertSame(spanContext, span.spanContext)
    }
}
