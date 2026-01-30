package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import okio.Buffer
import okio.use

private const val NANOS_PER_DAY = 24 * 60 * 60 * 1_000_000_000L
private const val UID_LENGTH = 16
private const val UID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"

private fun generateUid(): String {
    return (1..UID_LENGTH).map { UID_CHARS.random() }.joinToString("")
}

@OptIn(ExperimentalApi::class)
internal class TelemetryRepositoryImpl<T>(
    private val type: PersistedTelemetryType,
    private val config: PersistedTelemetryConfig,
    private val fileSystem: TelemetryFileSystem,
    private val serializer: (List<T>) -> ByteArray,
    private val deserializer: (ByteArray) -> List<T>,
    private val clock: Clock,
    private val uidGenerator: () -> String = ::generateUid,
) : TelemetryRepository<T> {

    override fun store(telemetry: List<T>): PersistedTelemetryRecord? {
        if (telemetry.isEmpty()) {
            return null
        }
        enforceStorageLimits()
        val record = PersistedTelemetryRecord(
            timestamp = clock.now(),
            type = type,
            uid = uidGenerator()
        )
        val bytes = try {
            serializer(telemetry)
        } catch (ignored: Exception) {
            return null
        }
        val source = Buffer().write(bytes)
        val success = fileSystem.write(record.filename, source)
        if (!success) {
            return null
        }
        return record
    }

    override fun poll(): PersistedTelemetryRecord? {
        return listRecordsForType()
            .minWithOrNull(PersistedTelemetryRecord.comparator)
    }

    override fun read(record: PersistedTelemetryRecord): List<T>? {
        val source = fileSystem.read(record.filename) ?: return null
        return try {
            source.use {
                deserializer(it.readByteArray())
            }
        } catch (ignored: Exception) {
            null
        }
    }

    override fun delete(record: PersistedTelemetryRecord) {
        fileSystem.delete(record.filename)
    }

    private fun listRecordsForType(): List<PersistedTelemetryRecord> {
        return fileSystem.list()
            .mapNotNull { PersistedTelemetryRecord.fromFilename(it) }
            .filter { it.type == type }
    }

    private fun enforceStorageLimits() {
        val records = listRecordsForType().sortedWith(PersistedTelemetryRecord.comparator)
        deleteExpiredRecords(records)
        deleteExcessRecords(records)
    }

    private fun deleteExpiredRecords(sortedRecords: List<PersistedTelemetryRecord>) {
        val maxAgeNanos = config.maxTelemetryAgeInDays * NANOS_PER_DAY
        val cutoffTime = clock.now() - maxAgeNanos
        sortedRecords
            .filter { it.timestamp < cutoffTime }
            .forEach { delete(it) }
    }

    private fun deleteExcessRecords(sortedRecords: List<PersistedTelemetryRecord>) {
        val validRecords = sortedRecords.filter { !isExpired(it) }
        if (validRecords.size >= config.maxBatchedItemsPerSignal) {
            val excess = validRecords.size - config.maxBatchedItemsPerSignal + 1
            validRecords.take(excess).forEach { delete(it) }
        }
    }

    private fun isExpired(record: PersistedTelemetryRecord): Boolean {
        val maxAgeNanos = config.maxTelemetryAgeInDays * NANOS_PER_DAY
        return record.timestamp < clock.now() - maxAgeNanos
    }
}
