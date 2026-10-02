package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(IncubatingApi::class)
internal class NodeOsTypeMapperTest {

    @Test
    internal fun `maps Node platforms to semantic convention types`() {
        val osTypes = mapOf(
            "aix" to OsAttributes.OsTypeValues.AIX.value,
            "android" to OsAttributes.OsTypeValues.LINUX.value,
            "linux" to OsAttributes.OsTypeValues.LINUX.value,
            "darwin" to OsAttributes.OsTypeValues.DARWIN.value,
            "freebsd" to OsAttributes.OsTypeValues.FREEBSD.value,
            "netbsd" to OsAttributes.OsTypeValues.NETBSD.value,
            "openbsd" to OsAttributes.OsTypeValues.OPENBSD.value,
            "sunos" to OsAttributes.OsTypeValues.SOLARIS.value,
            "win32" to OsAttributes.OsTypeValues.WINDOWS.value,
        )

        osTypes.forEach { (platform, expectedOsType) ->
            assertEquals(expectedOsType, NodeOsTypeMapper.map(platform))
        }
    }

    @Test
    internal fun `does not set type for unknown Node platform`() {
        assertNull(NodeOsTypeMapper.map("unknown"))
    }
}
