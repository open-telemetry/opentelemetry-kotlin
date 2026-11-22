package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResourceDetectionTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val factory = ResourceFactoryImpl(errorHandler)

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
    fun testDetectorWithThrowingNameIsReportedAndSkipped() {
        val exception = IllegalStateException("boom")
        val broken = FakeResourceDetector(attributes = mapOf("host.name" to "a"), nameError = exception)
        val detectors = listOf(
            broken,
            FakeResourceDetector(name = "working", attributes = mapOf("process.pid" to "123")),
        )

        val resource = detectors.detectResource(factory, errorHandler)

        assertEquals(mapOf("process.pid" to "123"), resource.attributes)
        assertEquals(0, broken.detectCount)

        val error = errorHandler.userCodeErrors.single()
        assertEquals(exception, error.cause)
        assertEquals("Failed to read resource detector name, skipping detector", error.message)
    }

    @Test
    fun testDetectorWithThrowingNameDoesNotPreventDuplicateNameReport() {
        val detectors = listOf(
            FakeResourceDetector(name = "host", attributes = mapOf("host.name" to "a")),
            FakeResourceDetector(nameError = IllegalStateException("boom")),
            FakeResourceDetector(name = "host", attributes = mapOf("host.arch" to "arm64")),
        )

        val resource = detectors.detectResource(factory, errorHandler)

        assertEquals(mapOf("host.name" to "a", "host.arch" to "arm64"), resource.attributes)
        assertEquals("Multiple resource detectors are named 'host'", errorHandler.apiMisuses.single().message)
        assertEquals(1, errorHandler.userCodeErrors.size)
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

    @Test
    fun testSameSchemaUrlNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = "https://example.com/1.0.0"
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = "https://example.com/1.0.0"
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals("https://example.com/1.0.0", resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testConflictingSchemaUrlsReported() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = "https://example.com/1.0.0"
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = "https://example.com/2.0.0"
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        // Keeps the last schema URL (process wins)
        assertEquals("https://example.com/2.0.0", resource.schemaUrl)

        val error = errorHandler.apiMisuses.single()
        assertEquals("ResourceDetector", error.api)
        assertTrue(error.message.contains("conflicting schema URL"))
        assertTrue(error.message.contains("https://example.com/1.0.0"))
        assertTrue(error.message.contains("https://example.com/2.0.0"))
        assertTrue(error.message.contains("process"))
    }

    @Test
    fun testEmptySchemaUrlNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = null
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = "https://example.com/1.0.0"
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals("https://example.com/1.0.0", resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testNullSchemaUrlNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = "https://example.com/1.0.0"
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = null
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals("https://example.com/1.0.0", resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testBothEmptySchemaUrlsNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = null
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = null
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertNull(resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testEmptyStringSchemaUrlNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = ""
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = "https://example.com/1.0.0"
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals("https://example.com/1.0.0", resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testBothEmptyStringSchemaUrlsNoConflict() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = ""
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = ""
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        assertEquals("", resource.schemaUrl)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testConflictingSchemaUrlsWithAttributesMerged() {
        val detectors = listOf(
            FakeResourceDetector(
                name = "host",
                attributes = mapOf("host.name" to "a"),
                schemaUrl = "https://example.com/1.0.0"
            ),
            FakeResourceDetector(
                name = "process",
                attributes = mapOf("process.pid" to "123"),
                schemaUrl = "https://example.com/2.0.0"
            ),
        )

        val resource = detectors.detectResource(factory, errorHandler)
        // Attributes should still be merged even with conflicting schema URLs
        assertEquals(mapOf("host.name" to "a", "process.pid" to "123"), resource.attributes)
        assertEquals("https://example.com/2.0.0", resource.schemaUrl)
        assertTrue(errorHandler.hasErrors())
    }
}
