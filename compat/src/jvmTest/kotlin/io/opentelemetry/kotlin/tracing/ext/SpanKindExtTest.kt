package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.SpanKind
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanKindExtTest {

    @Test
    fun toOtelJavaSpanKind() {
        val expected = mapOf(
            SpanKind.INTERNAL to OtelJavaSpanKind.INTERNAL,
            SpanKind.CLIENT to OtelJavaSpanKind.CLIENT,
            SpanKind.SERVER to OtelJavaSpanKind.SERVER,
            SpanKind.PRODUCER to OtelJavaSpanKind.PRODUCER,
            SpanKind.CONSUMER to OtelJavaSpanKind.CONSUMER,
        )
        expected.forEach {
            assertEquals(it.key.toOtelJavaSpanKind(), it.value)
        }
    }
}
