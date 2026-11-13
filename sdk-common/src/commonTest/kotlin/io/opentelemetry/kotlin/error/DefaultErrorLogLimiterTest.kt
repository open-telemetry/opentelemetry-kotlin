package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultErrorLogLimiterTest {
    private val warning = SdkErrorSeverity.WARNING

    @Test
    fun firstErrorAndExponentialSummaries() {
        val limiter = DefaultErrorLogLimiter()
        val error = SdkError.UserCodeError(IllegalStateException("oops"), "process", warning)
        val expected = listOf(
            DefaultErrorLogLimiter.Decision.Full,
            DefaultErrorLogLimiter.Decision.Repeated(1),
            DefaultErrorLogLimiter.Decision.Repeated(2),
            DefaultErrorLogLimiter.Decision.Skip,
            DefaultErrorLogLimiter.Decision.Repeated(4),
            DefaultErrorLogLimiter.Decision.Skip,
            DefaultErrorLogLimiter.Decision.Skip,
            DefaultErrorLogLimiter.Decision.Skip,
            DefaultErrorLogLimiter.Decision.Repeated(8),
        )
        assertEquals(expected, expected.indices.map { limiter.record(error) })
    }

    @Test
    fun repeatedThrowableSkipsRepeatedStackInspection() {
        var frameReads = 0
        val limiter = DefaultErrorLogLimiter(frameExtractor = {
            frameReads++
            "where"
        })
        val error = SdkError.UserCodeError(IllegalArgumentException("error"), "action", warning)
        assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(error))
        repeat(1000) { limiter.record(error) }
        assertEquals(1, frameReads)
    }

    @Test
    fun differentThrowablesFromSameSourceAreDeduplicated() {
        val limiter = DefaultErrorLogLimiter(frameExtractor = { "frame-one" })
        val first = SdkError.UserCodeError(IllegalStateException(), "operation", warning)
        val second = SdkError.UserCodeError(IllegalStateException(), "operation", warning)
        assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(first))
        assertEquals(DefaultErrorLogLimiter.Decision.Repeated(1), limiter.record(second))
    }

    @Test
    fun differentTopFramesAreSeparateErrors() {
        val limiter = DefaultErrorLogLimiter(frameExtractor = { it.message })
        val first = SdkError.UserCodeError(IllegalStateException("frame-a"), "operation", warning)
        val second = SdkError.UserCodeError(IllegalStateException("frame-b"), "operation", warning)
        assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(first))
        assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(second))
    }

    @Test
    fun keyIncludesSubtypeDetailsApiClassAndSeverity() {
        val limiter = DefaultErrorLogLimiter(frameExtractor = { "frame" })
        val uniqueErrors = listOf(
            SdkError.UserCodeError(IllegalStateException(), "operation", warning),
            SdkError.SdkCodeError(IllegalStateException(), "operation", warning),
            SdkError.UserCodeError(IllegalArgumentException(), "operation", warning),
            SdkError.UserCodeError(IllegalStateException(), "different", warning),
            SdkError.UserCodeError(IllegalStateException(), "operation", SdkErrorSeverity.ERROR),
            SdkError.ApiMisuse("Tracer", "operation", warning),
            SdkError.ApiMisuse("Meter", "operation", warning),
        )

        uniqueErrors.forEach { error ->
            assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(error))
        }
    }

    @Test
    fun capacityIsBoundedAndOverflowsAreSummarized() {
        val limiter = DefaultErrorLogLimiter(maxKeys = 1, frameExtractor = { "frame" })
        val first = SdkError.ApiMisuse("Tracer", "operation", warning)
        val overflow = SdkError.ApiMisuse("Meter", "operation", warning)
        assertEquals(DefaultErrorLogLimiter.Decision.Full, limiter.record(first))
        assertEquals(DefaultErrorLogLimiter.Decision.Overflow(1), limiter.record(overflow))
        assertEquals(DefaultErrorLogLimiter.Decision.Overflow(2), limiter.record(overflow))
        assertEquals(DefaultErrorLogLimiter.Decision.Skip, limiter.record(overflow))
        assertEquals(DefaultErrorLogLimiter.Decision.Overflow(4), limiter.record(overflow))
        assertEquals(DefaultErrorLogLimiter.Decision.Repeated(1), limiter.record(first))
    }
}
