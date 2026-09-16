package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.config.schema.model.IdGenerator
import io.opentelemetry.kotlin.config.schema.model.RandomIdGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class IdGeneratorMapperTest {

    @Test
    fun mapsRandom() {
        assertEquals(
            IdGeneratorBehavior.Random,
            IdGenerator(random = RandomIdGenerator()).toBehavior(),
        )
    }

    @Test
    fun leavesOmittedIdGeneratorUnset() {
        assertNull(IdGenerator().toBehavior())
    }
}
