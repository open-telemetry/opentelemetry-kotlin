package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class StdoutSpanExporterJvmTest {

    @Suppress("InjectDispatcher")
    @Test
    fun testInFlightExportCompletesWhenShutdownCalled() = runBlocking {
        val exportStarted = CountDownLatch(1)
        val proceedWithExport = CountDownLatch(1)
        val logOutput = mutableListOf<String>()

        val exporter = StdoutSpanExporter { line ->
            logOutput.add(line)
            exportStarted.countDown()
            proceedWithExport.await()
        }

        val span = FakeReadWriteSpan(name = "test-span")

        val exportJob = async(Dispatchers.Default) {
            exporter.export(listOf(span))
        }

        // Wait for the export to be inside the logger callback
        exportStarted.await()

        // Shutdown while the export is still inside the ifActive block
        exporter.shutdown()
        proceedWithExport.countDown()

        // The in-flight export should complete successfully
        assertEquals(OperationResultCode.Success, exportJob.await())
        assertEquals(1, logOutput.size)

        // New exports after shutdown should be rejected
        assertEquals(OperationResultCode.Failure, exporter.export(listOf(span)))
    }
}
