package io.opentelemetry.kotlin.factory

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
internal class IdGeneratorImpl(
    private val random: Random = Random.Default
) : IdGenerator {

    override fun generateTraceIdBytes(): ByteArray = generateId(TRACE_ID_BYTES)
    override fun generateSpanIdBytes(): ByteArray = generateId(SPAN_ID_BYTES)

    override val invalidTraceId: ByteArray = ByteArray(TRACE_ID_BYTES)
    override val invalidSpanId: ByteArray = ByteArray(SPAN_ID_BYTES)

    /**
     * Every byte is drawn from [Random], so the generated trace IDs satisfy the W3C Trace Context
     * Level 2 randomness requirements.
     */
    override val generatesRandomTraceIds: Boolean = true

    private fun generateId(length: Int): ByteArray {
        val bytes = ByteArray(length)
        do {
            random.nextBytes(bytes)
        } while (bytes.isAllZeroBytes()) // reject all-zero IDs
        return bytes
    }
}
