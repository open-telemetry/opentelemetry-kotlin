package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanExporter
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Exercises the adapter against a real opentelemetry-java component whose result completes on a
 * background thread, rather than a fake that completes synchronously.
 */
internal class OtelJavaBatchSpanProcessorFlushTest {

    @Test
    fun `test flush and shutdown of a real batch span processor`() = runBlocking {
        val impl = BatchSpanProcessor.builder(FakeOtelJavaSpanExporter()).build()
        val wrapper = impl.toOtelKotlinSpanProcessor()

        assertEquals(OperationResultCode.Success, wrapper.forceFlush())
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
    }
}
