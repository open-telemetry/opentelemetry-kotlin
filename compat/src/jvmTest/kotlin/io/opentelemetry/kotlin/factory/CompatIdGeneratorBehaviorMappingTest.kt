package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class CompatIdGeneratorBehaviorMappingTest {

    @Test
    fun mapsRandomBehavior() {
        assertIs<CompatIdGenerator>(IdGeneratorBehavior.Random.toIdGenerator())
    }

    @Test
    fun mapsCustomBehavior() {
        val custom = FakeIdGenerator()

        assertSame(custom, IdGeneratorBehavior.Custom { custom }.toIdGenerator())
    }
}
