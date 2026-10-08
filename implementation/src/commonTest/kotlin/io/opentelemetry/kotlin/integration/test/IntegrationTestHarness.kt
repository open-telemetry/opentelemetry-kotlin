package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.framework.OtelKotlinTestRule
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.random.Random

/**
 * Configures opentelemetry-kotlin to run for integration tests so that exported logs/traces
 * can be verified against expected output.
 */
internal class IntegrationTestHarness(
    scheduler: TestCoroutineScheduler,
    private val extraConfig: OpenTelemetryConfigDsl.() -> Unit = {},
) : OtelKotlinTestRule(scheduler) {
    override val kotlinApi: OpenTelemetry by lazy {
        createOpenTelemetry(
            clock = fakeClock,
            config = {
                idGenerator { IdGeneratorImpl(Random(0)) }
                tracerProvider { tracerProviderConfig() }
                loggerProvider { loggerProviderConfig() }
                extraConfig()
            },
        )
    }
}
