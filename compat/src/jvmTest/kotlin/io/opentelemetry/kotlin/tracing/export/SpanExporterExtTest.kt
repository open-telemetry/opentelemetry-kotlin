package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanExporter
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

internal class SpanExporterExtTest {

    @Test
    fun toOtelKotlinSpanExporter() = runTest {
        val impl = FakeOtelJavaSpanExporter()
        val adapter = impl.toOtelKotlinSpanExporter()
        adapter.export(mutableListOf(FakeSpanData()))

        val export = impl.exports.single()
        assertEquals("span", export.name)
    }

    @Test
    fun toOtelJavaApi() {
        val impl = FakeSpanExporter()
        val spans = mutableListOf<OtelJavaSpanData>(FakeOtelJavaSpanData(implName = "java-span"))

        val result = impl.toOtelJavaApi()
            .export(spans)
            .join(5, TimeUnit.SECONDS)

        assertEquals(true, result.isSuccess)
        assertEquals("java-span", impl.exports.single().name)
    }
}
