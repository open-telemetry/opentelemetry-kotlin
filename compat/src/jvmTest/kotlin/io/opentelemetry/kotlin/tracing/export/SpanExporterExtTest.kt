package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanExporter
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SpanExporterExtTest {

    @Test
    fun toOtelKotlinSpanExporter() = runTest {
        val impl = FakeOtelJavaSpanExporter()
        val adapter = impl.toOtelKotlinSpanExporter()
        adapter.export(mutableListOf(FakeSpanData()))

        val export = impl.exports.single()
        assertEquals("span", export.name)
    }
}
