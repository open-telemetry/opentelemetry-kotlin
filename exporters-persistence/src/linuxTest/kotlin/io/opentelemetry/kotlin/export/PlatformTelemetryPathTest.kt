package io.opentelemetry.kotlin.export

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformTelemetryPathTest {
    @Test
    fun testXdgCacheDirectoryTakesPrecedence() {
        assertEquals("/cache".toPath(), linuxCacheDirectory("/cache", "/home/user"))
    }

    @Test
    fun testMissingOrInvalidXdgCacheDirectoryUsesHome() {
        for (cacheHome in listOf(null, "", "relative/cache")) {
            assertEquals("/home/user/.cache".toPath(), linuxCacheDirectory(cacheHome, "/home/user"))
        }
    }

    @Test
    fun testMissingOrInvalidHomeUsesTemporaryDirectory() {
        for (home in listOf(null, "", "relative/home")) {
            assertEquals(FileSystem.SYSTEM_TEMPORARY_DIRECTORY, linuxCacheDirectory(null, home))
        }
    }
}
