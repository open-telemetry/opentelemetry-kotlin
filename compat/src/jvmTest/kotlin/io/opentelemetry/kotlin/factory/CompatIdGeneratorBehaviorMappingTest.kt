package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import org.junit.Test
import kotlin.test.assertIs

internal class CompatIdGeneratorBehaviorMappingTest {

    @Test
    fun mapsRandomBehavior() {
        assertIs<CompatIdGenerator>(IdGeneratorBehavior.Random.toIdGenerator())
    }
}
