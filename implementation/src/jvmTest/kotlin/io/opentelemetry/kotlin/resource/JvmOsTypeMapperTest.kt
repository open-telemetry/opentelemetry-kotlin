package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class JvmOsTypeMapperTest {

    @Test
    internal fun `maps JVM operating system names to semantic convention types`() {
        val osTypes = mapOf(
            "Windows 11" to "windows",
            "Linux" to "linux",
            "Mac OS X" to "darwin",
            "Darwin" to "darwin",
            "FreeBSD" to "freebsd",
            "NetBSD" to "netbsd",
            "OpenBSD" to "openbsd",
            "DragonFly BSD" to "dragonflybsd",
            "HP-UX" to "hpux",
            "AIX" to "aix",
            "SunOS" to "solaris",
            "z/OS" to "zos",
        )

        osTypes.forEach { (osName, expectedOsType) ->
            assertEquals(expectedOsType, JvmOsTypeMapper.map(osName))
        }
    }

    @Test
    internal fun `does not set type for unknown operating system`() {
        assertNull(JvmOsTypeMapper.map("Unknown"))
    }

    @Test
    internal fun `does not set type when operating system name is missing`() {
        assertNull(JvmOsTypeMapper.map(null))
    }
}
