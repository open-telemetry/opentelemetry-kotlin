package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class OpenTelemetryBehaviorTest {

    @Test
    fun startsWithDefaultValues() {
        val behavior = OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION)

        assertEquals(OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION, behavior.fileFormat)
        assertNull(behavior.disabled)
        assertNull(behavior.logLevel)
        assertNull(behavior.distribution)
        assertNull(behavior.entities)
        assertNull(behavior.resource)
        assertNull(behavior.attributeLimits)
        assertNull(behavior.tracerProvider)
        assertNull(behavior.loggerProvider)
    }

    @Test
    fun fileFormatAcceptsValidSupportedVersions() {
        listOf(
            "1.0",
            "1.1",
            "1.2",
            "1.0-rc.2",
            "1.2-beta.1",
        ).forEach {
            OpenTelemetryBehavior(fileFormat = it)
        }
    }

    @Test
    fun fileFormatRejectsUnsupportedVersions() {
        listOf(
            "1.3",
            "2.0",
            "2.1-rc.1",
        ).forEach { version ->
            assertFailsWith<IllegalArgumentException> {
                OpenTelemetryBehavior(fileFormat = version)
            }
        }
    }

    @Test
    fun fileFormatRejectsInvalidFormats() {
        listOf(
            "",
            "1",
            "1.",
            ".2",
            "1.2.3",
            "v1.2",
            "01.2",
            "1.02",
            "1.2-",
            "1.2+build.1",
        ).forEach { version ->
            assertFailsWith<IllegalArgumentException> {
                OpenTelemetryBehavior(fileFormat = version)
            }
        }
    }

    @Test
    fun mergingEmptyBehaviorChangesNothing() {
        val populated = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            disabled = true,
            logLevel = SeverityLevel.ERROR,
            distribution = mapOf("a" to 1),
            entities = "entities",
            resource = ResourceBehavior(attributes = mapOf("a" to 1L)),
            attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 1),
            loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsBehavior(attributeCountLimit = 1)),
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        assertEquals(
            populated,
            populated.mergeWith(
                OpenTelemetryBehavior(
                    fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION
                )
            )
        )
    }

    @Test
    fun mergingIntoEmptyBehaviorAdoptsEverything() {
        val populated = OpenTelemetryBehavior(
            fileFormat = "1.0",
            disabled = true,
            logLevel = SeverityLevel.ERROR,
            distribution = mapOf("a" to 1),
            entities = "entities",
            resource = ResourceBehavior(attributes = mapOf("a" to 1L)),
            attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 1),
            loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsBehavior(attributeCountLimit = 1)),
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        assertEquals(
            populated,
            OpenTelemetryBehavior(
                fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION
            ).mergeWith(populated)
        )
    }

    @Test
    fun staysUnsetWhenNoLayerConfiguresAnything() {
        assertEquals(
            OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION),
            OpenTelemetryBehavior(
                fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION
            ).mergeWith(OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION)),
        )
    }

    @Test
    fun mergeRecursesIntoNestedBlocks() {
        val merged = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(attributeCountLimit = 1, eventCountLimit = 4),
            ),
        ).mergeWith(
            OpenTelemetryBehavior(
                fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
                tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(eventCountLimit = 99)),
            ),
        )

        assertEquals(1, merged.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(99, merged.tracerProvider?.spanLimits?.eventCountLimit)
    }

    @Test
    fun mergesResourceAndTracingBranchesIndependently() {
        val resourceLayer = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            resource = ResourceBehavior(attributes = mapOf("service.namespace" to "shop")),
        )
        val tracingLayer = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            resource = ResourceBehavior(attributes = mapOf("deployment.environment.name" to "prod")),
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        val merged = resourceLayer.mergeWith(tracingLayer)

        assertEquals(
            mapOf("service.namespace" to "shop", "deployment.environment.name" to "prod"),
            merged.resource?.attributes,
        )
        assertEquals(3, merged.tracerProvider?.spanLimits?.linkCountLimit)
    }

    @Test
    fun mergesAttributeLimitsAndTracingBranchesIndependently() {
        val global = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            attributeLimits = AttributeLimitsBehavior(attributeCountLimit = 7),
        )
        val tracing = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        val merged = global.mergeWith(tracing)

        assertEquals(7, merged.attributeLimits?.attributeCountLimit)
        assertEquals(3, merged.tracerProvider?.spanLimits?.linkCountLimit)
    }

    @Test
    fun mergesTracingAndLoggingBranchesIndependently() {
        val tracing = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )
        val logging = OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsBehavior(attributeCountLimit = 7)),
        )

        val merged = tracing.mergeWith(logging)

        assertEquals(3, merged.tracerProvider?.spanLimits?.linkCountLimit)
        assertEquals(7, merged.loggerProvider?.logLimits?.attributeCountLimit)
    }

    @Test
    fun foldAppliesLayersInPrecedenceOrder() {
        val envLayer = configWithSpanLimits(
            SpanLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 2, linkCountLimit = 3),
        )
        val fileLayer = configWithSpanLimits(
            SpanLimitsBehavior(attributeCountLimit = 10, linkCountLimit = 30),
        )
        val dslLayer = configWithSpanLimits(SpanLimitsBehavior(attributeCountLimit = 100))

        val merged = mergeBehaviors(listOf(envLayer, fileLayer, dslLayer))

        val limits = merged.tracerProvider?.spanLimits
        assertEquals(100, limits?.attributeCountLimit)
        assertEquals(30, limits?.linkCountLimit)
        assertEquals(2, limits?.attributeValueLengthLimit)
    }

    @Test
    fun foldOfNoLayersIsEmpty() {
        assertEquals(
            OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION),
            mergeBehaviors(emptyList())
        )
    }

    @Test
    fun foldOfSingleLayerReturnsThatLayer() {
        val layer = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 3))

        assertEquals(layer, mergeBehaviors(listOf(layer)))
    }

    @Test
    fun foldIgnoresLayersThatConfiguredNothing() {
        val layer = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 3))
        val layers = listOf(
            OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION),
            layer,
            OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION)
        )

        assertEquals(layer, mergeBehaviors(layers))
    }

    private fun configWithSpanLimits(spanLimits: SpanLimitsBehavior) =
        OpenTelemetryBehavior(
            fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION,
            tracerProvider = TracerProviderBehavior(spanLimits = spanLimits)
        )
}
