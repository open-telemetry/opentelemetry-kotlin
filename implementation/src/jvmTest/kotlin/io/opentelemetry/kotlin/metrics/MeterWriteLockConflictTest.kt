package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that a write-lock registration conflict is reported through
 * [SdkErrorHandler] only after the registry lock has been released.
 *
 * The test forces a deterministic race: both competing registrations pass their
 * read-lock lookup before either is allowed to acquire the write lock, so the
 * loser is guaranteed to hit the write-lock recheck rather than the read lookup.
 */
internal class MeterWriteLockConflictTest {

    @Test
    fun writeLockConflictIsReportedOutsideRegistryLock() {
        val conflicts = CopyOnWriteArrayList<SdkError>()
        val workerErrors = CopyOnWriteArrayList<Throwable>()
        val probeFinished = CountDownLatch(1)
        val probeCompleted = AtomicBoolean(false)
        val probeFinishedDuringHandler = AtomicBoolean(false)

        var meter: Meter? = null
        val handler = SdkErrorHandler { error ->
            conflicts += error
            if (error is SdkError.ApiMisuse && error.message.contains("already registered")) {
                // Prove the handler is not running under the registry lock: this
                // worker must be able to acquire the meter lock and register a
                // different instrument while the handler is still executing.
                val currentMeter = meter ?: return@SdkErrorHandler
                Thread {
                    try {
                        currentMeter.createLongUpDownCounter(PROBE_INSTRUMENT)
                        probeCompleted.set(true)
                    } catch (t: Throwable) {
                        workerErrors += t
                    } finally {
                        probeFinished.countDown()
                    }
                }.apply { isDaemon = true }.start()

                // The probe must complete while this handler is still executing.
                // If the handler were invoked under the registry lock, the probe
                // could not acquire the meter lock and this await would time out.
                probeFinishedDuringHandler.set(
                    probeFinished.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                )
            }
        }

        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, AttributeLimitsBehavior())
        val meterInstance = provider.getMeter(name = "test")
        meter = meterInstance
        val meterImpl = meterInstance as MeterImpl

        // Both threads must observe a read-lock miss before either may proceed to
        // the write lock. The hook fires after the read lookup and before the write
        // lock acquisition inside getOrRegisterInstrument.
        val readMissed = CountDownLatch(2)
        val proceed = CountDownLatch(1)
        meterImpl.beforeWriteLock = { instrumentName ->
            if (instrumentName == INSTRUMENT_NAME) {
                readMissed.countDown()
                if (!proceed.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    throw AssertionError("timed out waiting for write-lock race release")
                }
            }
        }

        val longResult = AtomicReference<LongUpDownCounter?>()
        val doubleResult = AtomicReference<DoubleUpDownCounter?>()
        val callErrors = CopyOnWriteArrayList<Throwable>()

        fun launch(block: () -> Unit): Thread = Thread {
            try {
                block()
            } catch (t: Throwable) {
                callErrors += t
            }
        }.apply { isDaemon = true }

        val longThread = launch {
            longResult.set(meterInstance.createLongUpDownCounter(INSTRUMENT_NAME, UNIT, DESCRIPTION))
        }
        val doubleThread = launch {
            doubleResult.set(meterInstance.createDoubleUpDownCounter(INSTRUMENT_NAME, UNIT, DESCRIPTION))
        }

        try {
            longThread.start()
            doubleThread.start()

            // Wait until both threads have missed the read lookup, then release
            // them together to compete for the write lock.
            assertTrue(
                readMissed.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                "timed out waiting for both read-lock misses"
            )
            proceed.countDown()

            longThread.join(JOIN_TIMEOUT_MS)
            doubleThread.join(JOIN_TIMEOUT_MS)

            assertFalse(longThread.isAlive, "long registration thread did not finish")
            assertFalse(doubleThread.isAlive, "double registration thread did not finish")
            assertTrue(callErrors.isEmpty(), "registration calls threw: $callErrors")
        } finally {
            proceed.countDown()
            meterImpl.beforeWriteLock = null
        }

        // Exactly one conflict must be reported: the loser of the write-lock race.
        val conflictErrors = conflicts.filter {
            it is SdkError.ApiMisuse && it.message.contains("already registered")
        }
        assertEquals(1, conflictErrors.size, "expected exactly one write-lock conflict, got: $conflicts")
        val conflict = conflictErrors.single() as SdkError.ApiMisuse
        assertTrue(conflict.message.contains(INSTRUMENT_NAME))

        // Both registrations stay functional; the loser gets its own instrument.
        val longInstrument = assertNotNull(longResult.get())
        val doubleInstrument = assertNotNull(doubleResult.get())
        assertTrue(longInstrument.enabled())
        assertTrue(doubleInstrument.enabled())

        // The error handler ran outside the registry lock, so the probe
        // registration inside it must have completed within the bound while the
        // handler was still executing.
        assertTrue(
            probeFinishedDuringHandler.get(),
            "probe instrument registration timed out inside the error handler; " +
                "the handler may be running under the registry lock"
        )
        assertTrue(
            probeFinished.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS),
            "probe instrument registration did not finish"
        )
        assertTrue(probeCompleted.get(), "probe registration failed: $workerErrors")
        assertTrue(workerErrors.isEmpty(), "probe registration threw: $workerErrors")

        // Sanity: the probe instrument is registered and usable.
        assertNotNull(meterInstance.createLongUpDownCounter(PROBE_INSTRUMENT))
    }

    private companion object {
        const val INSTRUMENT_NAME = "my.counter"
        const val PROBE_INSTRUMENT = "lock.probe"
        const val UNIT = "1"
        const val DESCRIPTION = "test"
        const val LATCH_TIMEOUT_MS = 5_000L
        const val JOIN_TIMEOUT_MS = 5_000L
    }
}
