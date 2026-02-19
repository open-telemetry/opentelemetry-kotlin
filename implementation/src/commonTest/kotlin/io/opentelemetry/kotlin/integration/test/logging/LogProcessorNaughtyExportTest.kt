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

@OptIn(ExperimentalApi::class)
internal class LogProcessorNaughtyExportTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness =
            IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testOverridePropertiesInProcessor() = runTest {
        val processor = NaughtyLogRecordProcessor()
        prepareConfig(processor)
        val ctx = prepareContext()

        harness.logger.emit(
            body = "custom_log",
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
        harness.assertLogRecords(1, "log_naughty_export.json") {
            processor.overrideAttributes()
        }
    }

    private fun prepareConfig(processor: LogRecordProcessor) {
        harness.config.attributes = {
            setStringAttribute("resource.foo", "bar")
        }
        harness.config.schemaUrl = "https://example.com/foo"
        harness.config.logRecordProcessors.add(processor)
    }

    private fun prepareContext(): Context {
        val span = harness.tracer.startSpan("span")
        val contextFactory = harness.kotlinApi.contextFactory
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        return ctx
    }

    private class NaughtyLogRecordProcessor : LogRecordProcessor {

        private lateinit var log: ReadWriteLogRecord

        override fun onEmit(
            log: ReadWriteLogRecord,
            context: Context
        ) {
            this.log = log
        }

        fun overrideAttributes() {
            with(log) {
                body = "override"
                timestamp = 123
                observedTimestamp = 456
                severityNumber = SeverityNumber.INFO
                severityText = "info"
                setStringAttribute("key", "value")
            }
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
