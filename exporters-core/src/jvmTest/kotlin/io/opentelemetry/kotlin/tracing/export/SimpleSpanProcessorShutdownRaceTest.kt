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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Deterministic regression test proving that export admission and job registration are atomic
 * with respect to the shutdown snapshot: shutdown cannot observe a state where an accepted
 * export is missing from the pending-exports set.
 */
@Suppress("InjectDispatcher")
internal class SimpleSpanProcessorShutdownRaceTest {

    @Test
    fun testShutdownSnapshotCannotSplitAdmissionFromRegistration() {
        val exporter = GatedSpanExporter()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)
        val admissionEntered = CountDownLatch(1)
        val admissionRelease = CountDownLatch(1)
        processor.beforeJobStart = {
            admissionEntered.countDown()
            admissionRelease.await()
        }
        try {
            // onEnd admits the export and pauses inside the registration lock.
            val onEndThread = thread(isDaemon = true) {
                processor.onEnd(FakeReadWriteSpan(name = "span1"))
            }
            assertTrue(admissionEntered.await(5, TimeUnit.SECONDS), "onEnd did not reach admission")

            // Shutdown races the paused admission. Its pending-exports snapshot must block
            // on the registration lock, so exporter cleanup cannot run yet.
            val shutdownResult = CompletableDeferred<OperationResultCode>()
            thread(isDaemon = true) {
                shutdownResult.complete(runBlocking { processor.shutdown() })
            }

            assertFalse(
                exporter.shutdownCalled.await(500, TimeUnit.MILLISECONDS),
                "exporter shutdown ran while export admission was still in progress"
            )
            assertFalse(shutdownResult.isCompleted, "shutdown completed during paused admission")

            // Let admission finish: the job is registered and started, then blocks in export.
            admissionRelease.countDown()
            onEndThread.join(5_000)
            assertFalse(onEndThread.isAlive, "onEnd thread did not finish")

            // Shutdown now sees the registered job and must wait for the blocked export.
            assertFalse(
                exporter.shutdownCalled.await(500, TimeUnit.MILLISECONDS),
                "exporter shutdown ran while an accepted export was still in flight"
            )

            exporter.releaseExport()
            val result = runBlocking {
                withTimeout(10.seconds) { shutdownResult.await() }
            }

            assertEquals(OperationResultCode.Success, result)
            assertEquals(1, exporter.shutdownCalls.get())
            assertEquals(listOf("export:span1", "forceFlush", "shutdown"), exporter.events)

            // No export is admitted after shutdown.
            processor.onEnd(FakeReadWriteSpan(name = "span2"))
            assertEquals(1, exporter.exportCalls.get())
        } finally {
            processor.beforeJobStart = null
            exporter.releaseExport()
            admissionRelease.countDown()
        }
    }

    /** Exporter that blocks inside export until [releaseExport] completes. */
    private class GatedSpanExporter : SpanExporter {
        private val exportGate = CompletableDeferred<Unit>()
        private val eventLog = Collections.synchronizedList(mutableListOf<String>())
        val events: List<String> get() = eventLog.toList()
        val exportCalls = AtomicInteger(0)
        val shutdownCalls = AtomicInteger(0)
        val shutdownCalled = CountDownLatch(1)

        fun releaseExport() {
            exportGate.complete(Unit)
        }

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportCalls.incrementAndGet()
            exportGate.await()
            eventLog += "export:${telemetry.first().name}"
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode {
            eventLog += "forceFlush"
            return OperationResultCode.Success
        }

        override suspend fun shutdown(): OperationResultCode {
            eventLog += "shutdown"
            shutdownCalls.incrementAndGet()
            shutdownCalled.countDown()
            return OperationResultCode.Success
        }
    }
}
