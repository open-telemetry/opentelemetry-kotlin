package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class OtelJavaLogExportTest {

    private lateinit var harness: OtelKotlinHarness
    private val logger: OtelJavaLogger
        get() = harness.javaApi.logsBridge.get("test_logger")

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `test minimal log export`() = runTest {
        // logging without a body is allowed by the OTel spec, so we assert the MVP log here
        logger.logRecordBuilder().emit()

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_minimal.json",
        )
    }

    @Test
    fun `test log properties export`() = runTest {
        val logger = harness.javaApi.logsBridge.loggerBuilder("my_logger")
            .setInstrumentationVersion("0.1.0")
            .setSchemaUrl("https://example.com/schema")
            .build()

        logger.logRecordBuilder()
            .setBody("Hello, world!")
            .setTimestamp(100L, TimeUnit.NANOSECONDS)
            .setObservedTimestamp(50L, TimeUnit.NANOSECONDS)
            .setSeverity(OtelJavaSeverity.ERROR2)
            .setSeverityText("Error")
            .setAttribute("key2", "value2")
            .emit()

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_props.json",
        )
    }

    @Test
    fun `test java logger provider resource export`() = runTest {
        harness.config.apply {
            schemaUrl = "https://example.com/some_schema.json"
            attributes = {
                setStringAttribute("service.name", "test-service")
                setStringAttribute("service.version", "1.0.0")
                setStringAttribute("environment", "test")
            }
        }

        val javaLogger = harness.javaApi.logsBridge.get("test_logger")
        javaLogger.logRecordBuilder().setBody("Test log with custom resource").emit()

        harness.assertLogRecords(
            expectedCount = 1,
            goldenFileName = "log_resource.json",
        )
    }

    @Test
    fun `test context is passed to processor`() {
        // Create a processor that can capture the original Java context
        val javaContextCapturingProcessor = JavaContextCapturingProcessor()
        harness.config.logRecordProcessors.add(javaContextCapturingProcessor)

        // Create a context key and add a test value using Java API
        val javaContextKey = OtelJavaContextKey.named<String>("best_team")
        val testContextValue = "independiente"
        val javaContext = OtelJavaContext.current().with(javaContextKey, testContextValue)

        // Make the context current and emit log
        javaContext.makeCurrent().use {
            logger.logRecordBuilder().setBody("Test log with context").emit()
        }

        // Verify context was captured and contains expected value
        val actualValue = javaContextCapturingProcessor.capturedJavaContext?.get(javaContextKey)
        assertSame(testContextValue, actualValue)
    }

    /**
     * Custom processor that captures the original Java context from converted contexts
     */
    private class JavaContextCapturingProcessor : LogRecordProcessor {
        var capturedJavaContext: OtelJavaContext? = null

        override fun onEmit(log: ReadWriteLogRecord, context: Context) {
            capturedJavaContext = (context as ContextAdapter).impl
        }

        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    }
}
