package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class CreateOpenTelemetryConfigFileTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `a declared config file is accepted and ignored`() {
        val api = createOpenTelemetry {
            configFile("does-not-exist.yaml")
        }.also(sdks::track)
        assertNotNull(api)
    }
}
