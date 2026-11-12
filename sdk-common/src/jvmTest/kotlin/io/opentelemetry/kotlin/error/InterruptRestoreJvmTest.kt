package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InterruptRestoreJvmTest {
    @Test
    fun testUserCodeInterruptIsRestored() {
        val seen = mutableListOf<SdkError>()
        val handler = SdkErrorHandler { seen += it }
        try {
            Thread.interrupted()
            handler.guard { throw InterruptedException("stop") }
            assertTrue(Thread.currentThread().isInterrupted)
            assertEquals(1, seen.filterIsInstance<SdkError.UserCodeError>().size)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testWrappedUserCodeInterruptIsRestored() {
        val seen = mutableListOf<SdkError>()
        val handler = SdkErrorHandler { seen += it }
        try {
            Thread.interrupted()
            handler.sdkGuard { userCode { throw InterruptedException("stop") } }
            assertTrue(Thread.currentThread().isInterrupted)
            assertEquals(1, seen.filterIsInstance<SdkError.UserCodeError>().size)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testSdkCodeInterruptIsRestored() {
        val seen = mutableListOf<SdkError>()
        val handler = SdkErrorHandler { seen += it }
        try {
            Thread.interrupted()
            handler.sdkGuard { throw InterruptedException("stop") }
            assertTrue(Thread.currentThread().isInterrupted)
            assertEquals(1, seen.filterIsInstance<SdkError.SdkCodeError>().size)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testThrowingErrorHandlerRestoresInterrupt() {
        val handler = SdkErrorHandler { throw InterruptedException("stop") }
        try {
            Thread.interrupted()
            handler.reportError(SdkError.ApiMisuse("test", "test", SdkErrorSeverity.WARNING))
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testGuardedSdkErrorHandlerRestoresInterrupt() {
        val handler = GuardedSdkErrorHandler { throw InterruptedException("stop") }
        try {
            Thread.interrupted()
            handler.onError(SdkError.ApiMisuse("test", "test", SdkErrorSeverity.WARNING))
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testGuardedSdkErrorHandlerDoesNotInterruptForOtherFailures() {
        val handler = GuardedSdkErrorHandler { throw IllegalStateException("other") }
        try {
            Thread.interrupted()
            handler.onError(SdkError.ApiMisuse("test", "test", SdkErrorSeverity.WARNING))
            assertFalse(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testNonInterruptFailureDoesNotChangeFlag() {
        val handler = SdkErrorHandler { }
        try {
            Thread.interrupted()
            handler.guard { throw IllegalArgumentException("other") }
            assertFalse(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }
}
