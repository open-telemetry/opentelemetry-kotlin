package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SimpleLogRecordProcessorTest {

    @Test
    fun testSpanProcessorDefaults() = runTest {
        val processor =
            SimpleLogRecordProcessor(
                FakeLogRecordExporter()
            )
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testSpanProcessorExport() {
        val exporter = FakeLogRecordExporter()
        val processor =
            SimpleLogRecordProcessor(
                exporter
            )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        val export = exporter.logs.single()
        assertEquals(log.body, export.body)
    }
}
