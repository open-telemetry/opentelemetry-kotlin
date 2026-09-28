package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Asserts behaviour against the W3C `traceparent` spec:
 * https://www.w3.org/TR/trace-context/#traceparent-header
 */
@OptIn(ExperimentalApi::class)
internal class TraceParentTest {

    private val flagsFactory = TraceFlagsFactoryImpl()
    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val spanId = "b7ad6b7169203331"
    private val canonicalHeader = "00-$traceId-$spanId-01"
    private val traceFlags = TraceFlagsImpl(isSampled = true, isRandom = false)

    @Test
    fun `encode produces the canonical version 00 traceparent header`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = traceFlags,
        )
        assertNotNull(tp)
        assertEquals(canonicalHeader, tp.encode())
    }

    @Test
    fun `encoded header is exactly 55 chars for version 00`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = traceFlags,
        )
        assertNotNull(tp)
        assertEquals(55, tp.encode().length)
    }

    @Test
    fun `encode renders flags 00 when neither sampled nor random`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = TraceFlagsImpl(isSampled = false, isRandom = false),
        )
        assertNotNull(tp)
        assertEquals("00-$traceId-$spanId-00", tp.encode())
    }

    @Test
    fun `encode renders flags 02 when only random bit set`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = TraceFlagsImpl(isSampled = false, isRandom = true),
        )
        assertNotNull(tp)
        assertEquals("00-$traceId-$spanId-02", tp.encode())
    }

    @Test
    fun `encode renders flags 03 when sampled and random bits set`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = TraceFlagsImpl(isSampled = true, isRandom = true),
        )
        assertNotNull(tp)
        assertEquals("00-$traceId-$spanId-03", tp.encode())
    }

    @Test
    fun `encoded flags are always two lowercase hex characters`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = TraceFlagsImpl(isSampled = false, isRandom = false),
        )
        assertNotNull(tp)
        val flags = tp.encode().substringAfterLast('-')
        assertEquals(2, flags.length)
        assertTrue(flags.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `decode parses the canonical version 00 traceparent header`() {
        val tp = TraceParent.decode(canonicalHeader, flagsFactory)
        assertNotNull(tp)
        assertEquals("00", tp.version)
        assertEquals(traceId, tp.traceId)
        assertEquals(spanId, tp.spanId)
        assertTrue(tp.traceFlags.isSampled)
        assertFalse(tp.traceFlags.isRandom)
    }

    @Test
    fun `decode populates flags via the supplied factory`() {
        val tp = TraceParent.decode("00-$traceId-$spanId-03", flagsFactory)
        assertNotNull(tp)
        assertTrue(tp.traceFlags.isSampled)
        assertTrue(tp.traceFlags.isRandom)
    }

    @Test
    fun `decode handles flags 00 by reporting both flags off`() {
        val tp = TraceParent.decode("00-$traceId-$spanId-00", flagsFactory)
        assertNotNull(tp)
        assertFalse(tp.traceFlags.isSampled)
        assertFalse(tp.traceFlags.isRandom)
    }

    @Test
    fun `decode round-trips into encode without loss`() {
        val tp = TraceParent.decode(canonicalHeader, flagsFactory)
        assertNotNull(tp)
        assertEquals(canonicalHeader, tp.encode())
    }

    @Test
    fun `decode tolerates higher versions with extra fields per forward-compat rule`() {
        // the W3C spec requires forward-compatible parsing for unknown versions.
        // these headers may carry additional `-`-separated fields after flags,
        // so length and field count constraints from version 00 do not apply.
        val header = "01-$traceId-$spanId-01-extra"
        val tp = TraceParent.decode(header, flagsFactory)
        assertNotNull(tp)
        assertEquals("01", tp.version)
        assertEquals(traceId, tp.traceId)
        assertEquals(spanId, tp.spanId)
        assertTrue(tp.traceFlags.isSampled)
    }

    @Test
    fun `decode rejects an empty header`() {
        assertNull(TraceParent.decode("", flagsFactory))
    }

    @Test
    fun `decode rejects a header shorter than 55 chars`() {
        val header = "00-$traceId-${spanId.dropLast(1)}-01"
        assertTrue(header.length < 55)
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `decode rejects any uppercase character per spec`() {
        val uppercaseTraceId = traceId.replaceFirst('a', 'A')
        assertNull(TraceParent.decode("00-$uppercaseTraceId-$spanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects header with fewer than four fields`() {
        val header = "0".repeat(55)
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `decode rejects forbidden ff version per spec`() {
        assertNull(TraceParent.decode("ff-$traceId-$spanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects non-hex version`() {
        assertNull(TraceParent.decode("0g-$traceId-$spanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects version of wrong length`() {
        val header = "001-$traceId-$spanId-01"
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `decode rejects version 00 with an additional field`() {
        val header = "00-$traceId-$spanId-01-extra"
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `decode rejects trace id of wrong length`() {
        val longTrace = "0".repeat(33)
        assertNull(TraceParent.decode("01-$longTrace-$spanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects non-hex trace id`() {
        val invalidTraceId = traceId.replaceFirst('a', 'g')
        assertNull(TraceParent.decode("00-$invalidTraceId-$spanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects span id of wrong length`() {
        val longSpan = "0".repeat(17)
        assertNull(TraceParent.decode("01-$traceId-$longSpan-01", flagsFactory))
    }

    @Test
    fun `decode rejects non-hex span id`() {
        val invalidSpanId = spanId.replaceFirst('b', 'g')
        assertNull(TraceParent.decode("00-$traceId-$invalidSpanId-01", flagsFactory))
    }

    @Test
    fun `decode rejects flags of wrong length`() {
        assertNull(TraceParent.decode("01-$traceId-$spanId-001", flagsFactory))
    }

    @Test
    fun `decode rejects non-hex flags`() {
        assertNull(TraceParent.decode("00-$traceId-$spanId-zz", flagsFactory))
    }

    @Test
    fun `decode rejects header with uppercase version`() {
        val header = "0A-$traceId-$spanId-01"
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `decode rejects header with uppercase flags`() {
        val header = "00-$traceId-$spanId-0A"
        assertNull(TraceParent.decode(header, flagsFactory))
    }

    @Test
    fun `create returns a TraceParent for canonical inputs`() {
        val tp = TraceParent.create(
            version = "00",
            traceId = traceId,
            spanId = spanId,
            traceFlags = traceFlags,
        )
        assertNotNull(tp)
        assertEquals(canonicalHeader, tp.encode())
    }

    @Test
    fun `create returns null for version of wrong length`() {
        assertNull(TraceParent.create("0", traceId, spanId, traceFlags))
    }

    @Test
    fun `create returns null for non-hex version`() {
        assertNull(TraceParent.create("pp", traceId, spanId, traceFlags))
    }

    @Test
    fun `create returns null for forbidden version`() {
        assertNull(TraceParent.create("ff", traceId, spanId, traceFlags))
    }

    @Test
    fun `create returns null for trace id of wrong length`() {
        assertNull(TraceParent.create("00", "1234", spanId, traceFlags))
    }

    @Test
    fun `create returns null for non-hex trace id`() {
        assertNull(TraceParent.create("00", traceId.replaceFirst('a', 'g'), spanId, traceFlags))
    }

    @Test
    fun `create returns null for span id of wrong length`() {
        assertNull(TraceParent.create("00", traceId, "1234", traceFlags))
    }

    @Test
    fun `create returns null for non-hex span id`() {
        assertNull(TraceParent.create("00", traceId, spanId.replaceFirst('b', 'g'), traceFlags))
    }
}
