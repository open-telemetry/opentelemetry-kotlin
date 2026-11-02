package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import org.junit.Assert.assertEquals
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanExtTest {

    private val contextFactory = CompatContextFactory()
    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = createSpanContext(generator.generateTraceId(), generator.generateSpanId())

    @Test
    fun `test store span context valid`() {
        val span = contextFactory.root().storeSpanContext(validSpanContext).extractSpan()
        assertSpanContextsMatch(validSpanContext, span.spanContext)
    }

    @Test
    fun `test store span context invalid`() {
        val span = contextFactory.root().storeSpanContext(createInvalidSpanContext()).extractSpan()
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from context invalid`() {
        val span = contextFactory.root().extractSpan()
        assertSpanContextsMatch(createInvalidSpanContext(), span.spanContext)
    }

    @Test
    fun `test from context valid`() {
        val spanContext = OtelJavaSpanContext.create(
            generator.generateTraceId(),
            generator.generateSpanId(),
            OtelJavaTraceFlags.getDefault(),
            OtelJavaTraceState.getDefault()
        )
        val span = SpanAdapter(
            OtelJavaSpan.wrap(spanContext),
            OtelJavaContext.root(),
            SpanKind.INTERNAL,
            CompatSpanLimitsConfig(),
            NoopSdkErrorHandler,
        )
        val root = contextFactory.root()
        val ctx = span.storeInContext(root)
        val observed = root.extractSpan().spanContext
        assertSpanContextsMatch(createInvalidSpanContext(), observed)

        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(spanContext.toOtelKotlinSpanContext(), retrievedSpan.spanContext)
    }

    @Test
    fun `span attributes keep updating at the count limit and apply the value length limit`() {
        val limits = CompatSpanLimitsConfig().apply {
            attributeCountLimit = 1
            attributeValueLengthLimit = 3
        }
        val span = SpanAdapter(
            OtelJavaSpan.getInvalid(),
            OtelJavaContext.root(),
            SpanKind.INTERNAL,
            limits,
            NoopSdkErrorHandler,
        )

        span.setStringAttribute("first", "initial")
        span.setStringAttribute("first", "updated")
        span.setStringAttribute("second", "ignored")
        span.setStringListAttribute("strings", listOf("ignored"))
        span.setByteArrayAttribute("bytes", byteArrayOf(1, 2, 3, 4))
        span.setAnyValueAttribute(
            "value",
            AnyValue.MapValue(mapOf("nested" to AnyValue.StringValue("abcdef"))),
        )

        assertEquals(mapOf("first" to "upd"), span.attributes)
    }

    @Test
    fun `span mirrors truncate every supported value shape`() {
        val limits = CompatSpanLimitsConfig().apply {
            attributeCountLimit = 4
            attributeValueLengthLimit = 3
        }
        val span = SpanAdapter(
            OtelJavaSpan.getInvalid(),
            OtelJavaContext.root(),
            SpanKind.INTERNAL,
            limits,
            NoopSdkErrorHandler,
        )

        span.setStringAttribute("string", "abcdef")
        span.setStringListAttribute("strings", listOf("abcdef", "xy"))
        span.setByteArrayAttribute("bytes", byteArrayOf(1, 2, 3, 4))
        span.setAnyValueAttribute(
            "value",
            AnyValue.MapValue(
                mapOf(
                    "string" to AnyValue.StringValue("abcdef"),
                    "bytes" to AnyValue.BytesValue(byteArrayOf(1, 2, 3, 4)),
                ),
            ),
        )

        assertEquals("abc", span.attributes["string"])
        assertEquals(listOf("abc", "xy"), span.attributes["strings"])
        assertTrue((span.attributes["bytes"] as ByteArray).contentEquals(byteArrayOf(1, 2, 3)))
        assertEquals(
            AnyValue.MapValue(
                mapOf(
                    "string" to AnyValue.StringValue("abc"),
                    "bytes" to AnyValue.BytesValue(byteArrayOf(1, 2, 3)),
                ),
            ),
            span.attributes["value"],
        )
    }

    @Test
    fun `test store span context round trip`() {
        val root = contextFactory.root()
        val ctx = root.storeSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, ctx.extractSpan().spanContext)
        assertSpanContextsMatch(createInvalidSpanContext(), root.extractSpan().spanContext)
    }

    @Test
    fun `test store invalid span context`() {
        val ctx = contextFactory.root().storeSpanContext(createInvalidSpanContext())
        assertSpanContextsMatch(createInvalidSpanContext(), ctx.extractSpan().spanContext)
    }
}
