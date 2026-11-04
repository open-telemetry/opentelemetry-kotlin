@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.posix.PTHREAD_MUTEX_RECURSIVE
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_mutexattr_destroy
import platform.posix.pthread_mutexattr_init
import platform.posix.pthread_mutexattr_settype
import platform.posix.pthread_mutexattr_t
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class ReentrantReadWriteLock {
    @PublishedApi
    internal val mutex: CPointer<pthread_mutex_t> = createRecursiveMutex()

    @Suppress("unused")
    private val cleaner = createCleaner(mutex) {
        val result = pthread_mutex_destroy(it)
        if (result == 0) {
            nativeHeap.free(it)
        } else {
            // leak rather than free a mutex in an unknown state
            platformLog("pthread_mutex_destroy failed with code $result", SdkErrorSeverity.WARNING, null)
        }
    }

    public actual inline fun <T> write(action: () -> T): T {
        pthread_mutex_lock(mutex)
        try {
            return action()
        } finally {
            pthread_mutex_unlock(mutex)
        }
    }

    public actual inline fun <T> read(action: () -> T): T = write(action)
}

private fun createRecursiveMutex(): CPointer<pthread_mutex_t> = memScoped {
    val attributes = alloc<pthread_mutexattr_t>()
    checkPthreadResult(pthread_mutexattr_init(attributes.ptr), "pthread_mutexattr_init")
    try {
        checkPthreadResult(pthread_mutexattr_settype(attributes.ptr, PTHREAD_MUTEX_RECURSIVE.toInt()), "pthread_mutexattr_settype")
        val mutex = nativeHeap.alloc<pthread_mutex_t>().ptr
        val result = pthread_mutex_init(mutex, attributes.ptr)
        if (result != 0) {
            nativeHeap.free(mutex)
            error("pthread_mutex_init failed with code $result")
        }
        mutex
    } finally {
        val result = pthread_mutexattr_destroy(attributes.ptr)
        if (result != 0) {
            platformLog("pthread_mutexattr_destroy failed with code $result", SdkErrorSeverity.WARNING, null)
        }
    }
}

/**
 * Throws if a pthread call failed. Callers of the lock must not run without mutual exclusion.
 */
private fun checkPthreadResult(result: Int, operation: String) {
    check(result == 0) { "$operation failed with code $result" }
}
