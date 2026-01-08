package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.random.Random

/**
 * Generates random trace and span IDs.
 *
 * The implementation generates a random ByteArray of the correct size & then lazily constructs
 * a string if it's required (e.g. when the end-user retrieves the string via the API).
 *
 * In the happy path, a string will never need to be constructed & the library will happily
 * put the ByteArray directly in the Protobuf payload without any serialization/deserialization.
 */
@OptIn(ExperimentalApi::class)
internal class TracingIdFactoryImpl(
    private val random: Random = Random.Default
) : TracingIdFactory {

    private companion object {
        private const val TRACE_ID_BYTES = 16
        private const val SPAN_ID_BYTES = 8
    }

    override fun generateTraceIdBytes(): ByteArray = generateId(TRACE_ID_BYTES)
    override fun generateSpanIdBytes(): ByteArray = generateId(SPAN_ID_BYTES)

    override val invalidTraceId: ByteArray = ByteArray(TRACE_ID_BYTES)
    override val invalidSpanId: ByteArray = ByteArray(SPAN_ID_BYTES)

    private fun generateId(length: Int): ByteArray {
        val bytes = ByteArray(length)
        do {
            random.nextBytes(bytes)
        } while (bytes.all { it == 0.toByte() }) // reject all-zero IDs
        return bytes
    }
}
