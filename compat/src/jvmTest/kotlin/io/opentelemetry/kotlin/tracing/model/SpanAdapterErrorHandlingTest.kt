package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanAdapterErrorHandlingTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `throwing java span does not escape setters`() {
        val span = createSpan(ThrowingJavaSpan())
        span.setName("name")
        span.setStatus(StatusData.Error("error"))
        span.setBooleanAttribute("bool", true)
        span.setStringAttribute("string", "value")
        span.setLongAttribute("long", 1)
        span.setDoubleAttribute("double", 1.0)
        span.setBooleanListAttribute("bools", listOf(true))
        span.setStringListAttribute("strings", listOf("value"))
        span.setLongListAttribute("longs", listOf(1))
        span.setDoubleListAttribute("doubles", listOf(1.0))
        span.setByteArrayAttribute("bytes", byteArrayOf(1))
        span.setAnyValueAttribute("any", AnyValue.StringValue("value"))

        assertSdkErrors(
            listOf(
                "Span.setName failed",
                "Span.setStatus failed",
                "Span.setBooleanAttribute failed",
                "Span.setStringAttribute failed",
                "Span.setLongAttribute failed",
                "Span.setDoubleAttribute failed",
                "Span.setBooleanListAttribute failed",
                "Span.setStringListAttribute failed",
                "Span.setLongListAttribute failed",
                "Span.setDoubleListAttribute failed",
                "Span.setByteArrayAttribute failed",
                "Span.setAnyValueAttribute failed",
            )
        )
    }

    @Test
    fun `throwing java span makeCurrent returns a noop scope`() {
        val span = createSpan(ThrowingJavaSpan())
        val before = OtelJavaContext.current()
        val scope = span.makeCurrent()

        assertSame(OtelJavaScope.noop(), scope)
        assertSame(before, OtelJavaContext.current())
        scope?.close()
        assertSdkErrors(listOf("Span.makeCurrent failed"))
    }

    @Test
    fun `throwing caller supplied values are reported as user code errors`() {
        val span = createSpan(OtelJavaSpan.getInvalid())
        span.setBooleanListAttribute("bools", ThrowingList())
        span.setStringListAttribute("strings", ThrowingList())
        span.setLongListAttribute("longs", ThrowingList())
        span.setDoubleListAttribute("doubles", ThrowingList())
        span.setAnyValueAttribute("list", AnyValue.ListValue(ThrowingList()))
        span.setAnyValueAttribute("map", AnyValue.MapValue(mapOf("key" to AnyValue.ListValue(ThrowingList()))))

        assertEquals(
            listOf(
                "Span.setBooleanListAttribute failed",
                "Span.setStringListAttribute failed",
                "Span.setLongListAttribute failed",
                "Span.setDoubleListAttribute failed",
                "Span.setAnyValueAttribute failed",
                "Span.setAnyValueAttribute failed",
            ),
            errorHandler.userCodeErrors.map { it.message }
        )
        assertTrue(errorHandler.userCodeErrors.all { it.cause.message == "boom" })
        assertTrue(errorHandler.userCodeErrors.all { it.severity == SdkErrorSeverity.WARNING })
        assertEquals(0, errorHandler.sdkCodeErrors.size)
    }

    private fun createSpan(impl: OtelJavaSpan) = SpanAdapter(
        impl,
        null,
        SpanKind.INTERNAL,
        CompatSpanLimitsConfig(),
        errorHandler,
    )

    private fun assertSdkErrors(messages: List<String>) {
        assertEquals(messages.size, errorHandler.errors.size)
        assertEquals(messages, errorHandler.sdkCodeErrors.map { it.message })
        assertTrue(errorHandler.sdkCodeErrors.all { it.cause.message == "boom" })
        assertTrue(errorHandler.sdkCodeErrors.all { it.severity == SdkErrorSeverity.WARNING })
    }

    private class ThrowingList<T> : AbstractList<T>() {
        override val size: Int = 1
        override fun get(index: Int): T = error("boom")
    }

    private class ThrowingJavaSpan : OtelJavaSpan by OtelJavaSpan.getInvalid() {
        override fun <T : Any?> setAttribute(key: OtelJavaAttributeKey<T?>, value: T?): OtelJavaSpan = error("boom")
        override fun setAttribute(key: String, value: String?): OtelJavaSpan = error("boom")
        override fun setAttribute(key: String, value: Long): OtelJavaSpan = error("boom")
        override fun setAttribute(key: String, value: Double): OtelJavaSpan = error("boom")
        override fun setAttribute(key: String, value: Boolean): OtelJavaSpan = error("boom")
        override fun setStatus(statusCode: OtelJavaStatusCode, description: String): OtelJavaSpan = error("boom")
        override fun updateName(name: String): OtelJavaSpan = error("boom")
        override fun makeCurrent(): OtelJavaScope = error("boom")
    }
}
