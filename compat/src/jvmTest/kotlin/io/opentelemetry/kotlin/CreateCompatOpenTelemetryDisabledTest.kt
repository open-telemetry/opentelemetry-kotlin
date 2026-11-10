package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateCompatOpenTelemetryDisabledTest {

    @Test
    fun `disabled behavior creates the noop SDK`() {
        val api = createCompatOpenTelemetry {
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(disabled = true) }, "otel.yaml")
        }

        assertSame(NoopOpenTelemetrySdk, api)
    }

    @Test
    fun `enabled behavior creates the compat SDK implementation`() = runTest {
        val api = createCompatOpenTelemetry {
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(disabled = false) }, "otel.yaml")
        }

        assertNotSame(NoopOpenTelemetrySdk, api)
        api.shutdown()
    }
}
