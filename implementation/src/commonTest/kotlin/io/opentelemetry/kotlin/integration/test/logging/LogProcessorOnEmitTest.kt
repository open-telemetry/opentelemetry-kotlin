package io.opentelemetry.kotlin.integration.test.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class LogProcessorOnEmitTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness =
            IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testOverridePropertiesInProcessor() = runTest {
        prepareConfig()
        val ctx = prepareContext()

        harness.logger.emit(
            body = "custom_log",
            eventName = "my_event",
            timestamp = 500,
            observedTimestamp = 600,
            context = ctx,
            severityNumber = SeverityNumber.WARN2,
            severityText = "warn2",
            attributes = {
                setStringAttribute("foo", "bar")
                setBooleanAttribute("experiment_enabled", true)
            }
        )
        harness.assertLogRecords(1, "log_emit_override.json")
    }

    private fun prepareConfig() {
        harness.config.attributes = {
            setStringAttribute("resource.foo", "bar")
        }
        harness.config.schemaUrl = "https://example.com/foo"
        harness.config.logRecordProcessors.add(OnEmitLogRecordProcessor())
    }

    private fun prepareContext(): Context {
        val span = harness.tracer.createSpan("span")
        val contextFactory = harness.kotlinApi.contextFactory
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        return ctx
    }

    private class OnEmitLogRecordProcessor : LogRecordProcessor {
        override fun onEmit(
            log: ReadWriteLogRecord,
            context: Context
        ) {
            log.assertAttributes()
            log.overrideAttributes()
        }

        private fun ReadWriteLogRecord.assertAttributes() {
            assertEquals("my_event", eventName)
            assertEquals("custom_log", body)
            assertEquals(500, timestamp)
            assertEquals(600, observedTimestamp)
            assertEquals(SeverityNumber.WARN2, severityNumber)
            assertEquals("warn2", severityText)
            assertEquals("bar", attributes["foo"])
            assertEquals(true, attributes["experiment_enabled"])
            assertEquals("test_logger", instrumentationScopeInfo.name)
            assertEquals("bar", resource.attributes["resource.foo"])
            assertTrue(spanContext.isValid)
        }

        private fun ReadWriteLogRecord.overrideAttributes() {
            eventName = "override"
            body = "override"
            timestamp = 123
            observedTimestamp = 456
            severityNumber = SeverityNumber.INFO
            severityText = "info"
            setStringAttribute("key", "value")
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
