package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class ContextKeyRepositoryTest {

    @Test
    fun `adapter keys resolve to their wrapped key`() {
        val javaKey = OtelJavaContextKey.named<String>("key")
        assertSame(javaKey, ContextKeyRepository().get(ContextKeyAdapter(javaKey)))
    }

    @Test
    fun `foreign keys resolve to a stable key`() {
        val repository = ContextKeyRepository()
        val key1 = ForeignKey<String>()
        val key2 = ForeignKey<String>()
        assertSame(repository.get(key1), repository.get(key1))
        assertNotSame(repository.get(key1), repository.get(key2))
    }

    @Test
    fun `concurrent lookups of a new foreign key resolve to the same key`() {
        val repository = ContextKeyRepository()
        val key = ForeignKey<String>()
        val threads = 8
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threads)
        try {
            val results = (0 until threads).map {
                executor.submit<OtelJavaContextKey<String>> {
                    start.await()
                    repository.get(key)
                }
            }
            start.countDown()
            val keys = results.map { it.get(5, TimeUnit.SECONDS) }.toSet()
            assertEquals(1, keys.size)
        } finally {
            executor.shutdownNow()
        }
    }

    private class ForeignKey<T> : ContextKey<T>
}
