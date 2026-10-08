package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceDetector
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResourceDetectionConfigDslImplTest {

    @Test
    fun startsEmpty() {
        assertEquals(emptyList(), ResourceDetectionConfigDslImpl().detectors)
    }

    @Test
    fun detectorsKeepRegistrationOrder() {
        val first = NamedDetector("first")
        val second = NamedDetector("second")
        val dsl = ResourceDetectionConfigDslImpl().apply {
            detector(first)
            detector(second)
        }
        assertEquals(listOf(first, second), dsl.detectors)
    }

    @Test
    fun detectorsAreASnapshot() {
        val dsl = ResourceDetectionConfigDslImpl()
        val snapshot = dsl.detectors
        dsl.detector(NamedDetector("late"))
        assertEquals(emptyList(), snapshot)
    }

    private class NamedDetector(override val name: String) : ResourceDetector {
        override fun ResourceFactory.detect(): Resource = empty
    }
}
