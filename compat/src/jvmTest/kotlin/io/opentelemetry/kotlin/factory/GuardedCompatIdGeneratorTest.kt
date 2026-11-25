package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GuardedCompatIdGeneratorTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val ids = FakeIdGenerator()

    @Test
    fun `delegates without consulting the fallback`() {
        val fallback = CountingIdGenerator()
        val generator = GuardedCompatIdGenerator(ids, errorHandler, fallback)

        assertContentEquals(ids.generateTraceIdBytes(), generator.generateTraceIdBytes())
        assertContentEquals(ids.generateSpanIdBytes(), generator.generateSpanIdBytes())
        assertEquals(ids.generateTraceIdBytes().toHexString(), generator.generateTraceId())
        assertEquals(ids.generateSpanIdBytes().toHexString(), generator.generateSpanId())
        assertEquals(0, fallback.calls)
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun `falls back for each call that throws`() {
        val fallback = CountingIdGenerator()
        val generator = GuardedCompatIdGenerator(HostileIdGenerator, errorHandler, fallback)

        assertContentEquals(fallback.traceId, generator.generateTraceIdBytes())
        assertContentEquals(fallback.spanId, generator.generateSpanIdBytes())
        assertFalse(generator.generatesRandomTraceIds)
        assertEquals(fallback.traceId.toHexString(), generator.generateTraceId())
        assertEquals(fallback.spanId.toHexString(), generator.generateSpanId())
        assertFalse(generator.generatesRandomTraceIds())

        assertEquals(
            listOf(
                "generateTraceIdBytes",
                "generateSpanIdBytes",
                "generatesRandomTraceIds",
                "generateTraceId",
                "generateSpanId",
                "generatesRandomTraceIds",
            ).map { "IdGenerator.$it failed, using fallback. Later failures are not reported." },
            errorHandler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun `repeated failures are reported once per member`() {
        val fallback = CountingIdGenerator()
        val generator = GuardedCompatIdGenerator(HostileIdGenerator, errorHandler, fallback)

        repeat(1000) {
            assertEquals(fallback.spanId.toHexString(), generator.generateSpanId())
            assertContentEquals(fallback.spanId, generator.generateSpanIdBytes())
        }
        assertEquals(2000, fallback.calls)
        assertEquals(
            listOf("generateSpanId", "generateSpanIdBytes")
                .map { "IdGenerator.$it failed, using fallback. Later failures are not reported." },
            errorHandler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun `java methods of a delegate that implements the java interface are used`() {
        val generator = GuardedCompatIdGenerator(JavaAwareIdGenerator, errorHandler)

        assertEquals(JavaAwareIdGenerator.generateTraceId(), generator.generateTraceId())
        assertEquals(JavaAwareIdGenerator.generateSpanId(), generator.generateSpanId())
        assertTrue(generator.generatesRandomTraceIds())
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun `guarded skips sdk generators`() {
        val sdkGenerator = CompatIdGenerator()
        assertSame(sdkGenerator, sdkGenerator.guarded(errorHandler))
        val guarded = assertIs<GuardedCompatIdGenerator>(ids.guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }

    private class CountingIdGenerator : IdGenerator {
        val traceId = "abcdefabcdefabcdefabcdefabcdefab".hexToByteArray()
        val spanId = "abcdefabcdefabcd".hexToByteArray()
        var calls = 0

        override fun generateSpanIdBytes(): ByteArray = spanId.also { calls++ }
        override fun generateTraceIdBytes(): ByteArray = traceId.also { calls++ }
    }

    private object HostileIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = error("boom")
        override fun generateTraceIdBytes(): ByteArray = error("boom")
        override val generatesRandomTraceIds: Boolean get() = error("boom")
    }

    private object JavaAwareIdGenerator : IdGenerator, OtelJavaIdGenerator {
        override fun generateSpanIdBytes(): ByteArray = error("bytes should not be used")
        override fun generateTraceIdBytes(): ByteArray = error("bytes should not be used")
        override fun generateSpanId(): String = "0102030405060708"
        override fun generateTraceId(): String = "0102030405060708090a0b0c0d0e0f10"
        override fun generatesRandomTraceIds(): Boolean = true
    }
}
