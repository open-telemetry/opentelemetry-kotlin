package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResourceDetectionTest {

    private val factory = ResourceFactoryImpl()
    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun testNoDetectors() {
        val resource = emptyList<ResourceDetector>().detectResource(factory, errorHandler)

        assertTrue(resource.attributes.isEmpty())
        assertNull(resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testAttributesFromMultipleDetectors() {
        val detectors = listOf(
            FakeResourceDetector(name = "host", attributes = mapOf("host.name" to "my-host")),
            FakeResourceDetector(name = "process", attributes = mapOf("process.pid" to "123")),
        )

        val resource = detectors.detectResource(factory, errorHandler)

        assertEquals(mapOf("host.name" to "my-host", "process.pid" to "123"), resource.attributes)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testLaterDetectorWinsOnConflict() {
        val detectors = listOf(
            FakeResourceDetector(name = "first", attributes = mapOf("host.name" to "a")),
            FakeResourceDetector(name = "second", attributes = mapOf("host.name" to "b")),
        )

        assertEquals("b", detectors.detectResource(factory, errorHandler).attributes["host.name"])
        assertEquals("a", detectors.reversed().detectResource(factory, errorHandler).attributes["host.name"])
    }

    @Test
    fun testEmptyDetectorContributesNothing() {
        val detectors = listOf(
            FakeResourceDetector(name = "empty"),
            FakeResourceDetector(name = "host", attributes = mapOf("host.name" to "my-host")),
        )

        assertEquals(mapOf("host.name" to "my-host"), detectors.detectResource(factory, errorHandler).attributes)
    }

    @Test
    fun testSchemaUrlPropagates() {
        val detectors = listOf(
            FakeResourceDetector(attributes = mapOf("host.name" to "my-host"), schemaUrl = "https://example.com/1.0.0"),
        )

        assertEquals("https://example.com/1.0.0", detectors.detectResource(factory, errorHandler).schemaUrl)
    }

    @Test
    fun testThrowingDetectorIsReportedAndSkipped() {
        val exception = IllegalStateException("boom")
        val detectors = listOf(
            FakeResourceDetector(name = "broken", attributes = mapOf("host.name" to "a"), error = exception),
            FakeResourceDetector(name = "working", attributes = mapOf("process.pid" to "123")),
        )

        val resource = detectors.detectResource(factory, errorHandler)

        assertEquals(mapOf("process.pid" to "123"), resource.attributes)

        val error = errorHandler.userCodeErrors.single()
        assertEquals(exception, error.cause)
        assertEquals("Resource detector 'broken' failed", error.message)
    }

    @Test
    fun testDuplicateNamesAreReported() {
        val detectors = listOf(
            FakeResourceDetector(name = "host", attributes = mapOf("host.name" to "a")),
            FakeResourceDetector(name = "host", attributes = mapOf("host.arch" to "arm64")),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals(mapOf("host.name" to "a", "host.arch" to "arm64"), resource.attributes)

        val error = errorHandler.apiMisuses.single()
        assertEquals("ResourceDetector", error.api)
        assertEquals("Multiple resource detectors are named 'host'", error.message)
    }

    @Test
    fun testUniqueNamesAreNotReported() {
        val detectors = listOf(
            FakeResourceDetector(name = "host"),
            FakeResourceDetector(name = "process"),
        )

        detectors.detectResource(factory, errorHandler)

        assertFalse(errorHandler.hasErrors())
    }
}
