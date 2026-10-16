@file:OptIn(ObsoleteWorkersApi::class)

package io.opentelemetry.kotlin

import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class LinuxConcurrencyTest {
    @Test
    fun threadLocalValuesAreIsolatedAndCleared() {
        val local = ThreadLocal<String>()
        local.set("main")
        val worker = Worker.start()
        try {
            worker.execute(TransferMode.SAFE, { local }) {
                assertNull(it.get())
                it.set("worker")
                assertEquals("worker", it.get())
            }.result
            assertEquals("main", local.get())
            worker.execute(TransferMode.SAFE, { local }) {
                assertEquals("worker", it.get())
                it.set(null)
                assertNull(it.get())
            }.result
        } finally {
            local.set(null)
            worker.requestTermination().result
        }
    }

    @Test
    fun concurrentUpdatesAreNotLost() {
        val counter = AtomicLong()
        val lock = ReentrantReadWriteLock()
        val values = mutableListOf<Int>()
        val workers = List(4) { Worker.start() }
        try {
            val jobs = workers.map { worker ->
                worker.execute(TransferMode.SAFE, { Triple(counter, lock, values) }) { (counter, lock, values) ->
                    repeat(1_000) {
                        counter.incrementAndGet()
                        lock.write { values.add(it) }
                    }
                }
            }
            jobs.forEach { it.result }
            assertEquals(4_000L, counter.get())
            assertEquals(4_000, values.size)
        } finally {
            workers.forEach { it.requestTermination().result }
        }
    }

    @Test
    fun lockIsReentrantAndReleasedAfterExceptions() {
        val lock = ReentrantReadWriteLock()
        assertEquals(42, lock.write { lock.read { lock.write { 42 } } })
        assertFailsWith<IllegalStateException> { lock.write { error("test") } }
        val worker = Worker.start()
        try {
            assertEquals(42, worker.execute(TransferMode.SAFE, { lock }) { it.write { 42 } }.result)
        } finally {
            worker.requestTermination().result
        }
    }
}
