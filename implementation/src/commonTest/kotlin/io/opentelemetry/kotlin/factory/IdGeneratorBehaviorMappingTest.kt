package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import kotlin.test.Test
import kotlin.test.assertIs

internal class IdGeneratorBehaviorMappingTest {

    @Test
    fun mapsRandomBehavior() {
        assertIs<IdGeneratorImpl>(IdGeneratorBehavior.Random.toIdGenerator())
    }
}
