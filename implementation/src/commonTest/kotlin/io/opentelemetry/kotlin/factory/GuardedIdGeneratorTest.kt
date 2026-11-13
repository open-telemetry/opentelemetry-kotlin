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
        assertContentEquals(ids.invalidTraceId, generator.invalidTraceId)
        assertContentEquals(ids.invalidSpanId, generator.invalidSpanId)
        assertFalse(generator.generatesRandomTraceIds)

        assertEquals(
            listOf(
                "generateTraceIdBytes",
                "generateSpanIdBytes",
                "invalidTraceId",
                "invalidSpanId",
                "generatesRandomTraceIds",
            ).map { "IdGenerator.$it failed, using fallback" },
            errorHandler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun testGuardedSkipsSdkGenerators() {
        val sdkGenerator = IdGeneratorImpl()
        assertSame(sdkGenerator, sdkGenerator.guarded(errorHandler))
        val guarded = assertIs<GuardedIdGenerator>(ids.guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }

    private object HostileIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = error("boom")
        override fun generateTraceIdBytes(): ByteArray = error("boom")
        override val invalidTraceId: ByteArray get() = error("boom")
        override val invalidSpanId: ByteArray get() = error("boom")
        override val generatesRandomTraceIds: Boolean get() = error("boom")
    }
}
