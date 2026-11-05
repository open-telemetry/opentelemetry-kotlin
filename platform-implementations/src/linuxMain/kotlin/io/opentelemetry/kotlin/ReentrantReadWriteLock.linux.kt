@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package io.opentelemetry.kotlin

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
    // null if the mutex could not be created, in which case actions run without locking
    @PublishedApi
    internal val mutex: CPointer<pthread_mutex_t>? = createRecursiveMutex()

    @Suppress("unused")
    private val cleaner = mutex?.let {
        createCleaner(it) { pointer ->
            // leak rather than free a mutex in an unknown state
            if (pthreadSucceeded(pthread_mutex_destroy(pointer), "pthread_mutex_destroy")) {
                nativeHeap.free(pointer)
            }
        }
    }

    public actual inline fun <T> write(action: () -> T): T {
        val pointer = mutex ?: return action()
        pthread_mutex_lock(pointer)
        try {
            return action()
        } finally {
            pthread_mutex_unlock(pointer)
        }
    }

    public actual inline fun <T> read(action: () -> T): T = write(action)
}

private fun createRecursiveMutex(): CPointer<pthread_mutex_t>? = memScoped {
    val attributes = alloc<pthread_mutexattr_t>()
    if (!pthreadSucceeded(pthread_mutexattr_init(attributes.ptr), "pthread_mutexattr_init")) {
        return@memScoped null
    }
    try {
        val typeResult = pthread_mutexattr_settype(attributes.ptr, PTHREAD_MUTEX_RECURSIVE.toInt())
        if (!pthreadSucceeded(typeResult, "pthread_mutexattr_settype")) {
            return@memScoped null
        }
        val mutex = nativeHeap.alloc<pthread_mutex_t>().ptr
        if (!pthreadSucceeded(pthread_mutex_init(mutex, attributes.ptr), "pthread_mutex_init")) {
            nativeHeap.free(mutex)
            return@memScoped null
        }
        mutex
    } finally {
        pthreadSucceeded(pthread_mutexattr_destroy(attributes.ptr), "pthread_mutexattr_destroy")
    }
}

/**
 * Logs a warning if a pthread call failed. Returns true if it succeeded.
 */
private fun pthreadSucceeded(result: Int, operation: String): Boolean {
    if (result != 0) {
        platformLog("$operation failed with code $result", PlatformLogLevel.WARNING, null)
    }
    return result == 0
}
