package io.opentelemetry.kotlin.config

import kotlin.test.Test
import kotlin.test.assertNull

internal class PlatformEnvVarReaderTest {

    @Test
    fun `should not read envars on Apple platforms`() {
        assertNull(platformEnvVarReader())
    }
}
