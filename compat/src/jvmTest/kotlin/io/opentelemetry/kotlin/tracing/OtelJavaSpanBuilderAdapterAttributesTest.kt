package io.opentelemetry.kotlin.tracing

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtelJavaSpanBuilderAdapterAttributesTest {

    private val tracer = ActionRunningTracer()

    @Test
    fun `builder attributes keep their types`() {
        OtelJavaSpanBuilderAdapter(tracer, "span")
            .setAttribute("d", 1.0)
            .setAttribute(AttributeKey.longKey("l"), 1L)
            .setAttribute(AttributeKey.doubleArrayKey("dl"), listOf(1.0, 2.0))
            .setAttribute(AttributeKey.valueKey("v"), Value.of(mapOf("k" to Value.of("x"))))
            .startSpan()

        val attrs = tracer.span.attributes
        assertEquals(1.0, attrs["d"])
        assertEquals(1L, attrs["l"])
        assertEquals(listOf(1.0, 2.0), attrs["dl"])
        assertEquals(AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("x"))), attrs["v"])
    }

    @Test
    fun `link attributes keep their types`() {
        val linkAttrs = OtelJavaAttributes.builder()
            .put("d", 2.0)
            .put(AttributeKey.booleanArrayKey("bl"), listOf(true))
            .build()
        OtelJavaSpanBuilderAdapter(tracer, "span")
            .addLink(FakeSpanContext.VALID.toOtelJavaSpanContext(), linkAttrs)
            .startSpan()

        val link = tracer.span.links.single()
        assertEquals(mapOf("d" to 2.0, "bl" to listOf(true)), link.attributes)
    }

    private class ActionRunningTracer : Tracer {
        val span = FakeSpan()

        override fun enabled(): Boolean = true

        override fun startSpan(
            name: String,
            parentContext: Context?,
            spanKind: SpanKind,
            startTimestamp: Long,
            action: (SpanCreationAction.() -> Unit)?
        ): Span {
            val creation = object : SpanCreationAction, AttributesMutator by span, SpanLinkCreator by span {}
            action?.invoke(creation)
            return span
        }
    }
}
