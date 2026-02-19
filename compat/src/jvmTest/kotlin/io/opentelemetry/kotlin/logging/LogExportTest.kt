package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class LogExportTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `test enabled returns true`() {
        val logger = harness.kotlinApi.loggerProvider.getLogger("test-logger")
        assertTrue(logger.enabled())
    }

    @Test
    fun `test minimal log export`() = runTest {
        // logging without a body is allowed by the OTel spec, so we assert the MVP log here
        harness.logger.emit(null)

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_minimal.json",
        )
    }

    @Test
    fun `test log properties export`() = runTest {
        val logger = harness.kotlinApi.loggerProvider.getLogger(
            name = "my_logger",
            version = "0.1.0",
            schemaUrl = "https://example.com/schema",
        ) {
            setStringAttribute("key1", "value1")
        }
        logger.emit(
            body = "Hello, world!",
            timestamp = 100L,
            observedTimestamp = 50L,
            severityNumber = SeverityNumber.ERROR2,
            severityText = "Error",
            attributes = { setStringAttribute("key2", "value2") }
        )

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_props.json",
        )
    }

    @Test
    fun `test logger provider resource export`() = runTest {
        harness.config.apply {
            schemaUrl = "https://example.com/some_schema.json"
            attributes = {
                setStringAttribute("service.name", "test-service")
                setStringAttribute("service.version", "1.0.0")
                setStringAttribute("environment", "test")
            }
        }

        val logger = harness.kotlinApi.loggerProvider.getLogger("test_logger")
        logger.emit(body = "Test log with custom resource")

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_resource.json",
        )
    }

    @Test
    fun `test context is passed to processor`() {
        // Create a LogRecordProcessor that captures any passed Context.
        val contextCapturingProcessor = ContextCapturingProcessor()
        harness.config.logRecordProcessors.add(contextCapturingProcessor)

        // Create a context key and add a test value
        val currentContext = harness.kotlinApi.contextFactory.implicitContext()
        val contextKey = currentContext.createKey<String>("best_team")
        val testContextValue = "independiente"
        val testContext = currentContext.set(contextKey, testContextValue)

        // Log a message with the created context
        harness.logger.emit(
            body = "Test log with context",
            context = testContext
        )

        // Verify context was captured and contains expected value
        val actualValue = contextCapturingProcessor.capturedContext?.get(contextKey)
        assertSame(testContextValue, actualValue)
    }

    @Test
    fun `test log limit export`() = runTest {
        harness.config.logLimits = {
            attributeCountLimit = 2
            attributeValueLengthLimit = 3
        }
        harness.logger.emit(body = "Test log limits", attributes = {
            setStringAttribute("key1", "max")
            setStringAttribute("key2", "over_max")
            setStringAttribute("key3", "another")
        })
        harness.assertLogRecords(1, "log_limits.json")
    }

    @Test
    fun `test log export with custom processor`() = runTest {
        harness.config.logRecordProcessors.add(CustomLogRecordProcessor())
        harness.logger.emit("Test")

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_custom_processor.json",
        )
    }

    @Test
    fun `test event export`() = runTest {
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

    /**
     * Custom processor that captures the context passed to onEmit
     */
    private class ContextCapturingProcessor : LogRecordProcessor {
        var capturedContext: Context? = null
            private set

        override fun onEmit(log: ReadWriteLogRecord, context: Context) {
            capturedContext = context
        }

        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    }

    /**
     * Custom processor that alters log records
     */
    private class CustomLogRecordProcessor : LogRecordProcessor {

        override fun onEmit(log: ReadWriteLogRecord, context: Context) {
            with(log) {
                timestamp = 5
                observedTimestamp = 10

                setStringAttribute("string", "value")
                setBooleanAttribute("bool", false)
                setDoubleAttribute("double", 5.4)
                setLongAttribute("long", 5L)
                setStringListAttribute("stringList", listOf("value"))
                setBooleanListAttribute("boolList", listOf(false))
                setDoubleListAttribute("doubleList", listOf(5.4))
                setLongListAttribute("longList", listOf(5L))

                // these cannot be set in OTel Java.
                severityNumber = SeverityNumber.ERROR2
                severityText = "bad_error"
                body = "altered"
            }
        }

        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    }
}
