package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class CompatIdGeneratorBehaviorMappingTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun mapsRandomBehavior() {
        assertIs<CompatIdGenerator>(IdGeneratorBehavior.Random.toIdGenerator(errorHandler))
    }

    @Test
    fun mapsCustomBehavior() {
        val custom = FakeIdGenerator()

        assertSame(custom, IdGeneratorBehavior.Custom { custom }.toIdGenerator(errorHandler))
    }

    @Test
    fun throwingCustomSupplierFallsBackToDefault() {
        val behavior = IdGeneratorBehavior.Custom { error("boom") }

        assertIs<CompatIdGenerator>(behavior.toIdGenerator(errorHandler))
        val error = errorHandler.userCodeErrors.single()
        assertEquals("Failed to create IdGenerator, using default", error.message)
        assertEquals("boom", error.cause.message)
    }
}
