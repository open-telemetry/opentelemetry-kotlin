package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.uname
import platform.posix.utsname

@OptIn(ExperimentalForeignApi::class, IncubatingApi::class)
internal actual fun detectHostResourceAttributes(): HostResourceAttributes = memScoped {
    val info = alloc<utsname>()
    check(uname(info.ptr) == 0) { "Failed to read Linux host information" }
    HostResourceAttributes(
        osType = OsAttributes.OsTypeValues.LINUX.value,
        osName = info.sysname.toKString().takeIf(String::isNotBlank),
        osVersion = info.release.toKString().takeIf(String::isNotBlank),
    )
}
