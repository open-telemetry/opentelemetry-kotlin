package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class TelemetryExporterTest {

    private lateinit var exporter: TelemetryExporter<String>

    @BeforeTest
    fun setup() {
        exporter = TelemetryExporter(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            exportAction = { OtlpResponse.Success }
        )
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Failure, exporter.export(listOf("data")))
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testExportSucceedsBeforeShutdown() {
        assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
    }
}
