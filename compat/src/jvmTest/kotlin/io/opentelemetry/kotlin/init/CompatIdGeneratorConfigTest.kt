package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class CompatIdGeneratorConfigTest {

    private val clock = FakeClock()
    private val randomBehavior = OpenTelemetryBehavior(
        tracerProvider = TracerProviderBehavior(idGenerator = IdGeneratorBehavior.Random),
    )

    @Test
    fun usesDefaultIdGeneratorWhenBehaviorIsUnset() {
        val cfg = CompatOpenTelemetryConfig(clock)
        val idGenerator = CompatSdkConfigFactory(cfg, OpenTelemetryBehavior(), clock).idGenerator

        assertIs<CompatIdGenerator>(idGenerator)
    }

    @Test
    fun usesIdGeneratorFromResolvedBehavior() {
        val cfg = CompatOpenTelemetryConfig(clock)
        val idGenerator = CompatSdkConfigFactory(cfg, randomBehavior, clock).idGenerator

        assertIs<CompatIdGenerator>(idGenerator)
    }

    @Test
    fun customIdGeneratorTakesPrecedenceOverResolvedBehavior() {
        val custom = FakeIdGenerator()
        val cfg = CompatOpenTelemetryConfig(clock).apply {
            idGenerator { custom }
        }
        val idGenerator = CompatSdkConfigFactory(cfg, randomBehavior, clock).idGenerator

        assertSame(custom, idGenerator)
    }
}
