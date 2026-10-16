package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_REALTIME
import platform.posix.clock_gettime
import platform.posix.timespec

@OptIn(ExperimentalForeignApi::class)
public actual fun getCurrentTimeNanos(): Long = memScoped {
    val timestamp = alloc<timespec>()
    if (clock_gettime(CLOCK_REALTIME, timestamp.ptr) != 0) {
        platformLog("Failed to read the system clock", SdkErrorSeverity.ERROR, null)
        return@memScoped 0L
    }
    timestamp.tv_sec * 1_000_000_000L + timestamp.tv_nsec
}
