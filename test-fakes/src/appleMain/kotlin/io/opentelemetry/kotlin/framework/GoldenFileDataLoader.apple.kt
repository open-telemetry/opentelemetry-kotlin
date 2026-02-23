package io.opentelemetry.kotlin.framework

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

// see https://developer.squareup.com/blog/kotlin-multiplatform-shared-test-resources/
actual fun loadTestFixture(fixtureName: String): String {
    val mainBundle = NSBundle.mainBundle.bundlePath()
    val path = "$mainBundle/resources/$fixtureName"
    val data = checkNotNull(NSData.dataWithContentsOfFile(path)) {
        "Failed to successfully call dataWithContentsOfFile at $path"
    }
    return data.toByteArray().decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}
