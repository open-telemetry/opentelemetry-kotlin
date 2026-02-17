package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetryImpl
import io.opentelemetry.kotlin.factory.SdkFactoryImpl
import io.opentelemetry.kotlin.factory.TracingIdFactoryImpl
import io.opentelemetry.kotlin.framework.OtelKotlinTestRule
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.random.Random

/**
 * Configures opentelemetry-kotlin to run for integration tests so that exported logs/traces
 * can be verified against expected output.
 */
@OptIn(ExperimentalApi::class)
internal class IntegrationTestHarness(scheduler: TestCoroutineScheduler) : OtelKotlinTestRule(scheduler) {
    override val kotlinApi: OpenTelemetry by lazy {
        createOpenTelemetryImpl(
            clock = fakeClock,
            config = {
                tracerProvider { tracerProviderConfig() }
                loggerProvider { loggerProviderConfig() }
            },
            sdkFactory = SdkFactoryImpl(tracingIdFactory = TracingIdFactoryImpl(Random(0)))
        )
    }
}
