package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeSpanProcessorTest {

    private val fakeSpan = FakeReadWriteSpan()
    private val fakeContext = FakeContext()
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
    }

    @Test
    fun testNoSpanProcessors() = runTest {
        val processor =
            CompositeSpanProcessor(
                emptyList(),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertFalse(errorHandler.hasErrors())
        assertTrue(processor.isStartRequired())
        assertTrue(processor.isEndRequired())
    }

    @Test
    fun testProcessorNotInvoked() = runTest {
        val impl = FakeSpanProcessor(startRequired = false, endRequired = false)
        val other = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    impl,
                    other
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTrue(impl.startCalls.isEmpty())
        assertTrue(impl.endingCalls.isEmpty())
        assertTrue(impl.endCalls.isEmpty())
        assertEquals(1, other.startCalls.size)
        assertEquals(1, other.endingCalls.size)
        assertEquals(1, other.endCalls.size)
    }

    @Test
    fun testMultipleSpanProcessors() = runTest {
        val first = FakeSpanProcessor()
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorFlushFails() = runTest {
        val first = FakeSpanProcessor(flushCode = { Failure })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Failure,
            Success,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorShutdownFails() = runTest {
        val first = FakeSpanProcessor(shutdownCode = { Failure })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Failure,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorsThrowsInOnStart() = runTest {
        val first = FakeSpanProcessor(startAction = { _, _ -> throw IllegalStateException() })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorsThrowsInOnEnding() = runTest {
        val first = FakeSpanProcessor(endingAction = { throw IllegalStateException() })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorsThrowsInOnEnd() = runTest {
        val first = FakeSpanProcessor(endAction = { throw IllegalStateException() })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorsThrowsInFlush() = runTest {
        val first = FakeSpanProcessor(flushCode = { throw IllegalStateException() })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Failure,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorsThrowsInShutdown() = runTest {
        val first = FakeSpanProcessor(shutdownCode = { throw IllegalStateException() })
        val second = FakeSpanProcessor()
        val processor =
            CompositeSpanProcessor(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Failure,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    private suspend fun CompositeSpanProcessor.assertReturnValuesMatch(
        flush: OperationResultCode,
        shutdown: OperationResultCode
    ) {
        assertEquals(shutdown, shutdown())
        assertEquals(flush, forceFlush())
        onStart(fakeSpan, fakeContext)
        onEnding(fakeSpan)
        onEnd(fakeSpan)
    }

    private fun assertTelemetryCapturedSuccess(
        first: FakeSpanProcessor,
        second: FakeSpanProcessor
    ) {
        assertFalse(errorHandler.hasErrors())
        assertSame(fakeSpan, first.startCalls.single())
        assertSame(fakeSpan, first.endingCalls.single())
        assertSame(fakeSpan, first.endCalls.single())
        assertSame(fakeSpan, second.startCalls.single())
        assertSame(fakeSpan, second.endingCalls.single())
        assertSame(fakeSpan, second.endCalls.single())
    }

    private fun assertTelemetryCapturedFailure(
        first: FakeSpanProcessor,
        second: FakeSpanProcessor
    ) {
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertSame(fakeSpan, first.startCalls.single())
        assertSame(fakeSpan, first.endingCalls.single())
        assertSame(fakeSpan, first.endCalls.single())
        assertSame(fakeSpan, second.startCalls.single())
        assertSame(fakeSpan, second.endingCalls.single())
        assertSame(fakeSpan, second.endCalls.single())
    }
}
