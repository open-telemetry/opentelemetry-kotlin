package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class IdGeneratorBehaviorMappingTest {

    @Test
    fun mapsRandomBehavior() {
        assertIs<IdGeneratorImpl>(IdGeneratorBehavior.Random.toIdGenerator())
    }

    @Test
    fun mapsCustomBehavior() {
        val custom = FakeIdGenerator()

        assertSame(custom, IdGeneratorBehavior.Custom { custom }.toIdGenerator())
    }
}
