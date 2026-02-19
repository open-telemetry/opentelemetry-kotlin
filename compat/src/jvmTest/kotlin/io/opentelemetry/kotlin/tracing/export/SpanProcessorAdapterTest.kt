package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanProcessorAdapterTest {

    private lateinit var impl: FakeOtelJavaSpanProcessor
    private lateinit var wrapper: SpanProcessorAdapter

    @Before
    fun setUp() {
        impl = FakeOtelJavaSpanProcessor()
        wrapper = SpanProcessorAdapter(impl)
    }

    @Test
    fun `test shutdown returns success on second call`() = runTest {
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
    }

    @Test
    fun `test force flush works after shutdown`() = runTest {
        wrapper.shutdown()
        assertEquals(OperationResultCode.Success, wrapper.forceFlush())
    }
}
