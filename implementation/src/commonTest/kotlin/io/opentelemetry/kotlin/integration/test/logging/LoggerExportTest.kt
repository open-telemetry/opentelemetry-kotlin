package io.opentelemetry.kotlin.integration.test.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class LoggerExportTest {

    private val logAttributeLimit = 5
    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
            .apply {
                config.logLimits = {
                    attributeCountLimit = logAttributeLimit
                }
            }
    }

    @Test
    fun testMinimalLogExported() = runTest {
        harness.logger.emit("test") { setStringAttribute("foo", "bar") }
        harness.assertLogRecords(1, "log_minimal.json")
    }

    @Test
    fun testLogWithBasicPropertiesExported() = runTest {
        harness.logger.emit(
            body = "custom_log",
            timestamp = 500,
            observedTimestamp = 600,
            severityNumber = SeverityNumber.WARN2,
            severityText = "warn2"
        )
        harness.assertLogRecords(1, "log_basic_props.json")
    }

    @Test
    fun testLogWithAttributesExported() = runTest {
        harness.logger.emit("test") {
            setStringAttribute("foo", "bar")
            setBooleanAttribute("experiment_enabled", true)
        }
        harness.assertLogRecords(1, "log_attrs.json")
    }

    @Test
    fun testLogWithMetadataExported() = runTest {
        harness.config.attributes = {
            setStringAttribute("resource.foo", "bar")
        }
        harness.config.schemaUrl = "https://example.com/foo"
        val logger = harness.loggerProvider.getLogger("test", "0.1.0", "https://example.com/bar/") {
            setStringAttribute("instrumentation_scope.foo", "bar")
        }
        logger.emit("test")
        harness.assertLogRecords(1, "log_resource_scope.json")
    }

    @Test
    fun testLogWithParentSpanInContext() = runTest {
        val span = harness.tracer.createSpan("span")
        val contextFactory = harness.kotlinApi.contextFactory
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        harness.logger.emit("test", context = ctx)
        harness.assertLogRecords(1, "log_span_context.json")
    }

    @Test
    fun testLogWithRootContext() = runTest {
        val contextFactory = harness.kotlinApi.contextFactory
        val ctx = contextFactory.root()
        harness.logger.emit("test", context = ctx)
        harness.assertLogRecords(1, "log_root_context.json")
    }

    @Test
    fun testAttributeLimitsOnLogExport() = runTest {
        harness.logger.emit("test") {
            repeat(logAttributeLimit + 1) {
                setStringAttribute("key-$it", "value")
            }
        }
        harness.assertLogRecords(expectedCount = 1) { logs ->
            assertEquals(logAttributeLimit, logs.single().attributes.size)
        }
    }

    @Test
    fun testEventExport() = runTest {
        val logger = harness.kotlinApi.loggerProvider.getLogger("test_logger")
        logger.emit(
            body = "Some Event",
            eventName = "my_event_name",
            severityNumber = SeverityNumber.WARN4,
            attributes = { setStringAttribute("key1", "value1") }
        )

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "event.json",
        )
    }
}
