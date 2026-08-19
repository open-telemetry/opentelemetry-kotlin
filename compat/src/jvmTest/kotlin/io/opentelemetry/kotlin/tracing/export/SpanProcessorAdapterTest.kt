package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
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

    @Test
    fun `test flush awaits an async result`() = runTest {
        val pending = OtelJavaCompletableResultCode()
        impl.nextResult = { pending }

        val result = async { wrapper.forceFlush() }
        runCurrent()

        pending.succeed()
        assertEquals(OperationResultCode.Success, result.await())
    }

    @Test
    fun `test flush times out if the result never completes`() = runTest {
        impl.nextResult = { OtelJavaCompletableResultCode() }
        assertEquals(OperationResultCode.Failure, wrapper.forceFlush())
    }

    @Test
    fun `test exception thrown by wrapped processor does not propagate`() = runTest {
        impl.nextResult = { throw IllegalStateException("boom") }

        assertEquals(OperationResultCode.Failure, wrapper.forceFlush())
        assertEquals(OperationResultCode.Failure, wrapper.shutdown())
    }
}
