package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonLogRecordExporterTest {

    @Test
    fun `should successfully shutdown exporter`() = runTest {
        //given
        val exporter = JsonLogRecordExporterImpl()

        //when
        val result = exporter.shutdown()

        //then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun `should successfully force flush exporter`() = runTest {
        //given
        val exporter = JsonLogRecordExporterImpl()

        //when
        val result = exporter.forceFlush()

        //then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun `should successfully shutdown on second call`() = runTest {
        //given
        val exporter = JsonLogRecordExporterImpl()

        //when
        val initialResult = exporter.shutdown()

        //then
        assertEquals(OperationResultCode.Success, initialResult)
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun `should successfully force flush after shutdown`() = runTest {
        //given
        val exporter = JsonLogRecordExporterImpl()

        //when
        exporter.shutdown()

        //then
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }
}
