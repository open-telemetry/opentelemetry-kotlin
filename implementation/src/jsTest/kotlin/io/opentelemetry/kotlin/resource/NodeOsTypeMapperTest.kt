package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class NodeOsTypeMapperTest {

    @Test
    internal fun `maps Node platforms to semantic convention types`() {
        val osTypes = mapOf(
            "aix" to "aix",
            "android" to "linux",
            "linux" to "linux",
            "darwin" to "darwin",
            "freebsd" to "freebsd",
            "netbsd" to "netbsd",
            "openbsd" to "openbsd",
            "sunos" to "solaris",
            "win32" to "windows",
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
