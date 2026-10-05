package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@Suppress("InjectDispatcher")
internal class BatchTelemetryProcessorConcurrencyTest {

    private val producers = 8
    private val perProducer = 2_000
    private val timeout = 10.seconds

    @Test
    fun testConcurrentProducersLoseNothing() = runBlocking {
        withTimeout(timeout) {
            val exported = ConcurrentLinkedQueue<Int?>()
            val inFlight = AtomicInteger(0)
            val maxInFlight = AtomicInteger(0)
            val processor = createProcessor(maxQueueSize = producers * perProducer, batchSize = 32) { batch ->
                maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), ::maxOf)
                exported.addAll(batch)
                inFlight.decrementAndGet()
            }
            val flusher = launch(Dispatchers.Default) { repeat(100) { processor.forceFlush() } }
            produce(processor)
            flusher.join()
            processor.shutdown()

            assertEquals(0, exported.count { it == null })
            assertEquals((0 until producers * perProducer).toList(), exported.filterNotNull().sorted())
            assertEquals(1, maxInFlight.get())
        }
    }

    @Test
    fun testFullQueueDropsWithoutBlockingProducers() = runBlocking {
        withTimeout(timeout) {
            val exported = AtomicInteger(0)
            val release = CompletableDeferred<Unit>()
            val processor = createProcessor(maxQueueSize = 100, batchSize = 10) { batch ->
                release.await()
                exported.addAndGet(batch.size)
            }
            produce(processor)
            release.complete(Unit)
            processor.shutdown()
            assertTrue(exported.get() <= 110)
        }
    }

    private fun produce(processor: BatchTelemetryProcessor<Int>) {
        (0 until producers).map { p ->
            thread(isDaemon = true) { repeat(perProducer) { processor.processTelemetry(p * perProducer + it) } }
        }.forEach {
            it.join(timeout.inWholeMilliseconds)
            assertFalse(it.isAlive, "producer blocked")
        }
    }

    private fun createProcessor(maxQueueSize: Int, batchSize: Int, onExport: suspend (List<Int>) -> Unit) =
        BatchTelemetryProcessor(
            config = BatchTelemetryConfig(
                maxQueueSize = maxQueueSize,
                scheduleDelayMs = 1,
                maxExportBatchSize = batchSize,
                sdkErrorHandler = NoopSdkErrorHandler,
            ),
            dispatcher = Dispatchers.Default,
        ) {
            onExport(it)
            OperationResultCode.Success
        }
}
