package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertNotNull

internal class CreateOpenTelemetryConfigFileTest {

    @Test
    fun `a declared config file is accepted and ignored`() {
        val api = createOpenTelemetry {
            configFile("does-not-exist.yaml")
        }
        assertNotNull(api)
    }
}
