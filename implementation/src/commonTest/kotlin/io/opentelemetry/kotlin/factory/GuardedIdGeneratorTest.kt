package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GuardedIdGeneratorTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val ids = FakeIdGenerator()

    @Test
    fun testDelegatesWhenHealthy() {
        val generator = GuardedIdGenerator(ids, errorHandler, IdGeneratorImpl())
        assertContentEquals(ids.generateTraceIdBytes(), generator.generateTraceIdBytes())
        assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun testFallsBackForEachCallThatThrows() {
        val generator = GuardedIdGenerator(HostileIdGenerator, errorHandler, ids)
        assertContentEquals(ids.generateTraceIdBytes(), generator.generateTraceIdBytes())
        assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())
        assertFalse(generator.generatesRandomTraceIds)

        assertEquals(
            listOf(
                "generateTraceIdBytes",
                "generateSpanIdBytes",
                "generatesRandomTraceIds",
            ).map { "IdGenerator.$it failed, using fallback. Later failures are not reported." },
            errorHandler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun testRepeatedFailuresReportOncePerMember() {
        val generator = GuardedIdGenerator(HostileIdGenerator, errorHandler, ids)
        repeat(1000) {
            assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())
        }
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testGuardedSkipsSdkGenerators() {
        val sdkGenerator = IdGeneratorImpl()
        assertSame(sdkGenerator, sdkGenerator.guarded(errorHandler))
        val guarded = assertIs<GuardedIdGenerator>(ids.guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }

    @Test
    fun testFallsBackWhenTraceIdIsWrongSize() {
        val generator = GuardedIdGenerator(ShortTraceIdGenerator, errorHandler, ids)
        assertContentEquals(ids.generateTraceIdBytes(), generator.generateTraceIdBytes())
        assertContentEquals(ShortTraceIdGenerator.generateSpanIdBytes(), generator.generateSpanIdBytes())

        assertEquals(
            1,
            errorHandler.apiMisuses.size,
            "the wrong-size trace ID is reported once, the healthy span ID is not",
        )
        assertTrue(
            errorHandler.apiMisuses.single().message.startsWith(
                "IdGenerator.generateTraceIdBytes returned an invalid ID",
            ),
        )
    }

    @Test
    fun testFallsBackWhenSpanIdIsAllZero() {
        val generator = GuardedIdGenerator(AllZeroSpanIdGenerator, errorHandler, ids)
        assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())

        assertEquals(
            1,
            errorHandler.apiMisuses.size,
        )
        assertTrue(
            errorHandler.apiMisuses.single().message.startsWith(
                "IdGenerator.generateSpanIdBytes returned an invalid ID (expected 8 non-zero bytes, got 8)",
            ),
        )
    }

    @Test
    fun testInvalidIdsAreReportedOncePerMember() {
        val generator = GuardedIdGenerator(AllZeroIdGenerator, errorHandler, ids)
        repeat(3) {
            assertContentEquals(ids.generateTraceIdBytes(), generator.generateTraceIdBytes())
            assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())
        }

        assertEquals(
            listOf(
                "IdGenerator.generateTraceIdBytes returned an invalid ID " +
                    "(expected 16 non-zero bytes, got 16), using fallback. " +
                    "Later invalid IDs from this member are not reported.",
                "IdGenerator.generateSpanIdBytes returned an invalid ID " +
                    "(expected 8 non-zero bytes, got 8), using fallback. " +
                    "Later invalid IDs from this member are not reported.",
            ),
            errorHandler.apiMisuses.map { it.message },
        )
    }

    private object HostileIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = error("boom")
        override fun generateTraceIdBytes(): ByteArray = error("boom")
        override val generatesRandomTraceIds: Boolean get() = error("boom")
    }

    private object ShortTraceIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = "1234561234561234".hexToByteArray()
        override fun generateTraceIdBytes(): ByteArray = "123456123456123412345612345612".hexToByteArray()
        override val generatesRandomTraceIds: Boolean = true
    }

    private object AllZeroSpanIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = ByteArray(8)
        override fun generateTraceIdBytes(): ByteArray = "12345612345612341234561234561234".hexToByteArray()
        override val generatesRandomTraceIds: Boolean = true
    }

    private object AllZeroIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = ByteArray(8)
        override fun generateTraceIdBytes(): ByteArray = ByteArray(16)
        override val generatesRandomTraceIds: Boolean = true
    }
}
