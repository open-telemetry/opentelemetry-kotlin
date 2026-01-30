package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.PersistedTelemetryType.LOGS
import okio.Buffer
import okio.BufferedSource
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class TelemetryRepositoryImplTest {

    private val fakeTelemetry = listOf(FakeTelemetryObject("item"))
    private lateinit var fileSystem: FakeTelemetryFileSystem
    private lateinit var clock: FakeClock
    private var uidCounter: Int = 0

    @BeforeTest
    fun setUp() {
        fileSystem = FakeTelemetryFileSystem()
        clock = FakeClock()
        uidCounter = 0
    }

    @Test
    fun testSingleStoreThenDelete() {
        val repository = createRepository()

        // store
        val record = repository.store(fakeTelemetry)
        assertNotNull(record)
        assertEquals(LOGS, record.type)
        assertEquals(clock.now(), record.timestamp)

        // poll
        val polled = checkNotNull(repository.poll())
        assertEquals(record, polled)

        // read
        val readTelemetry = repository.read(record)
        assertNotNull(readTelemetry)
        assertEquals(1, readTelemetry.size)
        assertEquals("item", readTelemetry[0].value)

        // delete
        repository.delete(record)
        assertNull(repository.poll())
    }

    @Test
    fun testMultiStoreThenDelete() {
        val repository = createRepository()
        val items = listOf("first", "second", "third")
        val records = mutableListOf<PersistedTelemetryRecord>()

        // store
        for (item in items) {
            incrementTelemetryTime()
            val record = repository.store(listOf(FakeTelemetryObject(item)))
            assertNotNull(record)
            records.add(record)
        }

        // poll, read, delete
        for ((index, item) in items.withIndex()) {
            val expected = records[index]
            assertEquals(expected, repository.poll())

            val telemetry = repository.read(expected)
            assertEquals(item, telemetry?.get(0)?.value)
            repository.delete(expected)
        }

        // no items remaining
        assertNull(repository.poll())
    }

    @Test
    fun testMaxTelemetryAgeInDays() {
        assertFailsWith<IllegalArgumentException> {
            PersistedTelemetryConfig(maxTelemetryAgeInDays = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            PersistedTelemetryConfig(maxTelemetryAgeInDays = -1)
        }
    }

    @Test
    fun testMaxBatchedItemsPerSignal() {
        assertFailsWith<IllegalArgumentException> {
            PersistedTelemetryConfig(maxBatchedItemsPerSignal = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            PersistedTelemetryConfig(maxBatchedItemsPerSignal = -1)
        }
    }

    @Test
    fun testEmptyFileSystem() {
        val repository = createRepository()
        assertNull(repository.poll())

        val fakeRecord = PersistedTelemetryRecord(
            timestamp = 1000L,
            type = LOGS,
            uid = "nonexistent"
        )
        assertNull(repository.read(fakeRecord))
        repository.delete(fakeRecord)
    }

    @Test
    fun testEmptyBatchNotStored() {
        val repository = createRepository()
        val record = repository.store(emptyList())
        assertNull(record)
        assertNull(repository.poll())
    }

    @Test
    fun testSerializerExceptionHandled() {
        val repository = createRepository(
            serializer = { error("Serialization failed") }
        )
        val record = repository.store(fakeTelemetry)
        assertNull(record)
        assertNull(repository.poll())
    }

    @Test
    fun testDeserializerExceptionHandled() {
        val repository = createRepository(
            deserializer = { error("Deserialization failed") }
        )
        val record = repository.store(fakeTelemetry)
        assertNotNull(record)
        assertNull(repository.read(record))
    }

    @Test
    fun testFilesystemWriteFailureHandled() {
        fileSystem.failWrites = true
        val repository = createRepository()
        val record = repository.store(fakeTelemetry)
        assertNull(record)
    }

    @Test
    fun testFilesystemReadFailureHandled() {
        fileSystem.failReads = true
        val repository = createRepository()
        fileSystem.failReads = false

        val record = repository.store(fakeTelemetry)
        assertNotNull(record)

        fileSystem.failReads = true
        assertNull(repository.read(record))
    }

    @Test
    fun testFilesystemDeleteFailureHandled() {
        val repository = createRepository()
        val record = repository.store(fakeTelemetry)
        assertNotNull(record)
        fileSystem.failDeletes = true

        repository.delete(record)
        assertNotNull(repository.poll())
    }

    @Test
    fun testMaxStorageLimitExceeded() {
        val config = PersistedTelemetryConfig(maxBatchedItemsPerSignal = 3)
        val repository = createRepository(config = config)

        val items = listOf("first", "second", "third", "fourth")
        for (item in items) {
            incrementTelemetryTime()
            repository.store(listOf(FakeTelemetryObject(item)))
        }

        // oldest telemetry should have been deleted.
        val polled = repository.poll()
        assertNotNull(polled)
        assertEquals(2 * TIME_INCREMENT_MS, polled.timestamp)

        val telemetry = repository.read(polled)
        assertEquals("second", telemetry?.get(0)?.value)
    }

    @Test
    fun testOldTelemetryPolicy() {
        val config = PersistedTelemetryConfig(maxTelemetryAgeInDays = 7)
        val repository = createRepository(config = config)

        repository.store(listOf(FakeTelemetryObject("a")))
        advanceClockByDays(8)
        val expected = repository.store(listOf(FakeTelemetryObject("b")))

        // oldest telemetry (8 days) pruned
        val polled = checkNotNull(repository.poll())
        assertEquals(expected, polled)
        repository.delete(polled)
        assertNull(repository.poll())
    }

    @Test
    fun testMultipleTelemetryItemsInBatch() {
        val repository = createRepository()
        val batch = listOf(
            FakeTelemetryObject("item1"),
            FakeTelemetryObject("item2"),
            FakeTelemetryObject("item3")
        )

        val record = repository.store(batch)
        assertNotNull(record)

        val readBatch = repository.read(record)
        assertNotNull(readBatch)
        assertEquals(3, readBatch.size)
        assertEquals("item1", readBatch[0].value)
        assertEquals("item2", readBatch[1].value)
        assertEquals("item3", readBatch[2].value)
    }

    @Test
    fun testPollReturnsOldestByTimestamp() {
        val repository = createRepository()

        incrementTelemetryTime()
        val record1 = repository.store(fakeTelemetry)

        incrementTelemetryTime()
        repository.store(fakeTelemetry)

        incrementTelemetryTime()
        repository.store(fakeTelemetry)
        assertEquals(record1, repository.poll())
    }

    private class FakeTelemetryObject(val value: String)

    /**
     * A fake implementation of [TelemetryFileSystem] for testing.
     *
     * This is used instead of [TelemetryFileSystemImpl] with okio's FakeFileSystem because:
     * 1. TelemetryFileSystemImpl uses gzip compression, adding unnecessary complexity to tests
     * 2. This fake provides explicit controls for simulating failure scenarios (failWrites,
     *    failReads, failDeletes) which are difficult to achieve with the real implementation
     */
    private class FakeTelemetryFileSystem : TelemetryFileSystem {
        private val files = mutableMapOf<String, ByteArray>()
        var failWrites = false
        var failReads = false
        var failDeletes = false

        override fun write(filename: String, source: BufferedSource): Boolean {
            if (failWrites) {
                return false
            } else {
                files[filename] = source.readByteArray()
                return true
            }
        }

        override fun list(): List<String> = files.keys.toList()

        override fun delete(filename: String) {
            if (failDeletes) {
                return
            } else {
                files.remove(filename)
            }
        }

        override fun read(filename: String): BufferedSource? {
            if (failReads) {
                return null
            }
            val bytes = files[filename] ?: return null
            return Buffer().write(bytes)
        }
    }

    private fun createRepository(
        config: PersistedTelemetryConfig = PersistedTelemetryConfig(),
        serializer: (List<FakeTelemetryObject>) -> ByteArray = { telemetry ->
            telemetry.joinToString(DELIMITER) { it.value }.encodeToByteArray()
        },
        deserializer: (ByteArray) -> List<FakeTelemetryObject> = { bytes ->
            bytes.decodeToString().split(DELIMITER).map { FakeTelemetryObject(it) }
        },
    ): TelemetryRepository<FakeTelemetryObject> {
        return TelemetryRepositoryImpl(
            type = LOGS,
            config = config,
            fileSystem = fileSystem,
            serializer = serializer,
            deserializer = deserializer,
            clock = clock,
            uidGenerator = { "id_${uidCounter++}" }
        )
    }

    private fun incrementTelemetryTime() {
        clock.time += TIME_INCREMENT_MS
    }

    private fun advanceClockByDays(days: Int) {
        clock.time += days * NANOS_PER_DAY
    }

    private companion object {
        const val NANOS_PER_DAY = 24 * 60 * 60 * 1_000_000_000L
        const val TIME_INCREMENT_MS = 1000L
        const val DELIMITER = ","
    }
}
