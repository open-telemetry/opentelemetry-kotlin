package io.opentelemetry.kotlin.propagation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun testDecodeKeepsAtMost32Members() {
        val header = (1..33).joinToString(",") { "k$it=v$it" }
        val decoded = W3CTraceStateCodec.decode(header)
        assertEquals((1..32).map { "k$it" }, decoded.keys.toList())
    }

    @Test
    fun testEncodeKeepsAtMost32Members() {
        val entries = (1..33).associate { "k$it" to "v$it" }
        val expected = (1..32).joinToString(",") { "k$it=v$it" }
        assertEquals(expected, W3CTraceStateCodec.encode(entries))
    }

    @Test
    fun testDecodeKeepsAHeaderOfExactly512Characters() {
        val header = exactly512Header()
        assertEquals(512, header.length)
        assertEquals(header, W3CTraceStateCodec.encode(W3CTraceStateCodec.decode(header)))
    }

    @Test
    fun testEncodeKeepsAHeaderOfExactly512Characters() {
        val header = exactly512Header()
        val entries = linkedMapOf(
            "a" to "x".repeat(254),
            "b" to "x".repeat(253),
        )
        assertEquals(header, W3CTraceStateCodec.encode(entries))
    }

    @Test
    fun testDecodeDropsMembersLongerThan128BeforeDroppingFromTheEnd() {
        assertEquals(
            truncatedOversizedHeader(),
            W3CTraceStateCodec.encode(W3CTraceStateCodec.decode(oversizedHeaderWithTrailingLargeMember())),
        )
    }

    @Test
    fun testEncodeDropsMembersLongerThan128BeforeDroppingFromTheEnd() {
        val entries = linkedMapOf("keep" to "ok")
        (0 until 4).forEach { entries["k$it"] = "x".repeat(126) }
        assertEquals(truncatedOversizedHeader(), W3CTraceStateCodec.encode(entries))
    }

    @Test
    fun testDecodeDropsMembersFromTheEndWhenAllMembersAreAtMost128Characters() {
        val members = (0 until 11).map { "a$it=" + "x".repeat(47) }
        val header = members.joinToString(",")
        assertTrue(header.length > 512)
        val encoded = W3CTraceStateCodec.encode(W3CTraceStateCodec.decode(header))
        assertTrue(encoded.length <= 512)
        assertEquals(members.dropLast(1).joinToString(","), encoded)
    }

    private fun exactly512Header(): String {
        val first = "a=" + "x".repeat(254)
        val second = "b=" + "x".repeat(253)
        return "$first,$second"
    }

    private fun oversizedHeaderWithTrailingLargeMember(): String {
        val small = "keep=ok"
        val large = (0 until 4).joinToString(",") { "k$it=" + "x".repeat(126) }
        return "$small,$large"
    }

    private fun truncatedOversizedHeader(): String {
        val small = "keep=ok"
        val large = (0 until 3).joinToString(",") { "k$it=" + "x".repeat(126) }
        return "$small,$large"
    }
}
