package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryDisabledTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `disabled behavior creates the noop SDK`() {
        val api = createOpenTelemetry {
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(disabled = true) }, "otel.yaml")
        }

        assertSame(NoopOpenTelemetrySdk, api)
    }

    @Test
    fun `enabled behavior creates the SDK implementation`() {
        val api = createOpenTelemetry {
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(disabled = false) }, "otel.yaml")
        }.also(sdks::track)

        assertNotSame(NoopOpenTelemetrySdk, api)
    }
}
