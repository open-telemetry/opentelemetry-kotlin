package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(IncubatingApi::class)
internal class JvmOsTypeMapperTest {

    @Test
    internal fun `maps JVM operating system names to semantic convention types`() {
        val osTypes = mapOf(
            "Windows 11" to OsAttributes.OsTypeValues.WINDOWS.value,
            "Linux" to OsAttributes.OsTypeValues.LINUX.value,
            "Mac OS X" to OsAttributes.OsTypeValues.DARWIN.value,
            "Darwin" to OsAttributes.OsTypeValues.DARWIN.value,
            "FreeBSD" to OsAttributes.OsTypeValues.FREEBSD.value,
            "NetBSD" to OsAttributes.OsTypeValues.NETBSD.value,
            "OpenBSD" to OsAttributes.OsTypeValues.OPENBSD.value,
            "DragonFly BSD" to OsAttributes.OsTypeValues.DRAGONFLYBSD.value,
            "HP-UX" to OsAttributes.OsTypeValues.HPUX.value,
            "AIX" to OsAttributes.OsTypeValues.AIX.value,
            "SunOS" to OsAttributes.OsTypeValues.SOLARIS.value,
            "z/OS" to OsAttributes.OsTypeValues.ZOS.value,
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
