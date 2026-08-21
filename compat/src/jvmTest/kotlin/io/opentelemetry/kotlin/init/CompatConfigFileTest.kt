package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.createCompatOpenTelemetry
import org.junit.Test
import kotlin.test.assertNotNull

internal class CompatConfigFileTest {

    @Test
    fun `a declared config file is accepted and ignored`() {
        val api = createCompatOpenTelemetry {
            configFile("does-not-exist.yaml")
        }
        assertNotNull(api)
    }
}
