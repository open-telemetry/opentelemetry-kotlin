package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class LogRecordProcessorAdapterTest {

    private lateinit var impl: FakeOtelJavaLogRecordProcessor
    private lateinit var wrapper: LogRecordProcessorAdapter

    @Before
    fun setUp() {
        impl = FakeOtelJavaLogRecordProcessor()
        wrapper = LogRecordProcessorAdapter(impl)
    }

    @Test
    fun `test shutdown returns success on second call`() = runTest {
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
    }

    @Test
    fun `test enabled returns false after shutdown`() = runTest {
        wrapper.shutdown()
        assertFalse(wrapper.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
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
