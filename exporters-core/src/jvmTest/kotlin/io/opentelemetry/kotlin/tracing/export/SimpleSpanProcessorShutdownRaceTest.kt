package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@Suppress("InjectDispatcher")
internal class SimpleSpanProcessorShutdownRaceTest {

    @Test
    fun testShutdownSnapshotCannotSplitAdmissionFromRegistration() {
        val exporter = GatedSpanExporter()
        val processor = SimpleSpanProcessor(
            exporter,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
            NoopSdkErrorHandler,
        )
        val admissionEntered = CountDownLatch(1)
        val admissionRelease = CountDownLatch(1)
        processor.beforeJobStart = {
            admissionEntered.countDown()
            admissionRelease.await()
        }
        try {
            val onEndThread = thread(isDaemon = true) {
                processor.onEnd(FakeReadWriteSpan(name = "span1"))
            }
            assertTrue(admissionEntered.await(5, TimeUnit.SECONDS), "onEnd did not reach admission")

            // Shutdown's snapshot must block on the registration lock, so cleanup cannot
            // run while admission is still in progress.
            val shutdownResult = CompletableDeferred<OperationResultCode>()
            thread(isDaemon = true) {
                shutdownResult.complete(runBlocking { processor.shutdown() })
            }
            assertFalse(
                exporter.shutdownCalled.await(500, TimeUnit.MILLISECONDS),
                "exporter shutdown ran while export admission was still in progress"
            )

            admissionRelease.countDown()
            onEndThread.join(5_000)
            assertFalse(onEndThread.isAlive, "onEnd thread did not finish")

            // Shutdown sees the registered job and must wait for the blocked export.
            assertFalse(
                exporter.shutdownCalled.await(500, TimeUnit.MILLISECONDS),
                "exporter shutdown ran while an accepted export was still in flight"
            )

            exporter.releaseExport()
            val result = runBlocking { withTimeout(10.seconds) { shutdownResult.await() } }

            assertEquals(OperationResultCode.Success, result)
            assertEquals(listOf("export:span1", "forceFlush", "shutdown"), exporter.events)

            processor.onEnd(FakeReadWriteSpan(name = "span2"))
            assertEquals(1, exporter.events.count { it.startsWith("export:") })
        } finally {
            processor.beforeJobStart = null
            exporter.releaseExport()
            admissionRelease.countDown()
        }
    }

    /** Exporter that blocks inside export until [releaseExport] completes. */
    private class GatedSpanExporter : SpanExporter {
        private val exportGate = CompletableDeferred<Unit>()
        val events = Collections.synchronizedList(mutableListOf<String>())
        val shutdownCalled = CountDownLatch(1)

        fun releaseExport() {
            exportGate.complete(Unit)
        }

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportGate.await()
            events += "export:${telemetry.first().name}"
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode {
            events += "forceFlush"
            return OperationResultCode.Success
        }

        override suspend fun shutdown(): OperationResultCode {
            events += "shutdown"
            shutdownCalled.countDown()
            return OperationResultCode.Success
        }
    }
}
