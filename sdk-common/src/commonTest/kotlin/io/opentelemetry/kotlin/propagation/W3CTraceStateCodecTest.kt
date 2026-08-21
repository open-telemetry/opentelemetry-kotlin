package io.opentelemetry.kotlin.propagation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Asserts the structure of the W3C `tracestate` list format:
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 */
internal class W3CTraceStateCodecTest {

    @Test
    fun testEncodeSingleListMember() {
        assertEquals("foo=bar", W3CTraceStateCodec.encode(mapOf("foo" to "bar")))
    }

    @Test
    fun testEncodeJoinsListMembersPreservingOrder() {
        val entries = linkedMapOf("foo" to "1", "bar" to "2", "baz" to "3")
        assertEquals("foo=1,bar=2,baz=3", W3CTraceStateCodec.encode(entries))
    }

    @Test
    fun testEncodeEmptyEntries() {
        assertEquals("", W3CTraceStateCodec.encode(emptyMap()))
    }

    @Test
    fun testEncodeMultiTenantKey() {
        val encoded = W3CTraceStateCodec.encode(mapOf("tenant@vendor" to "value"))
        assertEquals("tenant@vendor=value", encoded)
    }

    @Test
    fun testDecodeSingleListMember() {
        assertEquals(mapOf("foo" to "bar"), W3CTraceStateCodec.decode("foo=bar"))
    }

    @Test
    fun testDecodeMultipleListMembersPreservingOrder() {
        val decoded = W3CTraceStateCodec.decode("foo=1,bar=2,baz=3")
        assertEquals(listOf("foo", "bar", "baz"), decoded.keys.toList())
        assertEquals(mapOf("foo" to "1", "bar" to "2", "baz" to "3"), decoded)
    }

    @Test
    fun testDecodeMultiTenantKey() {
        assertEquals(mapOf("tenant@vendor" to "value"), W3CTraceStateCodec.decode("tenant@vendor=value"))
    }

    @Test
    fun testDecodeEmptyHeader() {
        assertEquals(emptyMap(), W3CTraceStateCodec.decode(""))
    }

    @Test
    fun testDecodeHeaderOfOnlyCommas() {
        assertEquals(emptyMap(), W3CTraceStateCodec.decode(","))
        assertEquals(emptyMap(), W3CTraceStateCodec.decode(",,,"))
    }

    @Test
    fun testDecodeHeaderOfOnlyWhitespace() {
        assertEquals(emptyMap(), W3CTraceStateCodec.decode("   \t  "))
    }

    @Test
    fun testDecodeTrimsSurroundingWhitespace() {
        val decoded = W3CTraceStateCodec.decode(" foo=bar ,\tbaz=qux\t")
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), decoded)
    }

    @Test
    fun testDecodeSkipsEmptyListMembers() {
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), W3CTraceStateCodec.decode("foo=bar,,baz=qux"))
    }

    @Test
    fun testDecodeSkipsMemberLackingEqualsSign() {
        assertEquals(emptyMap(), W3CTraceStateCodec.decode("nokey"))
        assertEquals(mapOf("foo" to "bar"), W3CTraceStateCodec.decode("foo=bar,nokey"))
        assertEquals(
            mapOf("foo" to "bar", "baz" to "qux"),
            W3CTraceStateCodec.decode("foo=bar,nokey,baz=qux")
        )
    }

    @Test
    fun testDecodeSkipsTrailingListMemberSeparator() {
        assertEquals(mapOf("a" to "1"), W3CTraceStateCodec.decode("a=1,"))
    }

    @Test
    fun testDecodeSkipsMemberWithEmptyKey() {
        assertEquals(mapOf("foo" to "bar"), W3CTraceStateCodec.decode("=value,foo=bar"))
    }

    @Test
    fun testDecodeSkipsMemberWithEmptyValue() {
        assertEquals(mapOf("bar" to "baz"), W3CTraceStateCodec.decode("foo=,bar=baz"))
    }

    @Test
    fun testDecodeSplitsOnTheFirstEqualsSign() {
        assertEquals(mapOf("k" to "v=w"), W3CTraceStateCodec.decode("k=v=w"))
    }

    @Test
    fun testDecodeKeepsFirstOccurrenceOfDuplicateKey() {
        assertEquals(mapOf("a" to "1"), W3CTraceStateCodec.decode("a=1,a=2"))
    }

    @Test
    fun testDecodeThenEncodeRoundTrips() {
        val header = "foo=1,bar=2,baz=3"
        assertEquals(header, W3CTraceStateCodec.encode(W3CTraceStateCodec.decode(header)))
    }
}
