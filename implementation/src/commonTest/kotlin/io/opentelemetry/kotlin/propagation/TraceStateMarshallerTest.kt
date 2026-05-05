package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Asserts behaviour against the W3C `tracestate` spec:
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 */
@OptIn(ExperimentalApi::class)
internal class TraceStateMarshallerTest {

    private val factory = TraceStateFactoryImpl()

    @Test
    fun `encode produces a single key=value list-member`() {
        val ts = TraceStateMarshaller(factory.default.put("foo", "bar"))
        assertEquals("foo=bar", ts.encode())
    }

    @Test
    fun `encode joins multiple list-members with comma and preserves insertion order`() {
        val state = factory.default
            .put("foo", "1")
            .put("bar", "2")
            .put("baz", "3")
        val ts = TraceStateMarshaller(state)
        assertEquals("foo=1,bar=2,baz=3", ts.encode())
    }

    @Test
    fun `encode of an empty state returns an empty string`() {
        val ts = TraceStateMarshaller(factory.default)
        assertEquals("", ts.encode())
    }

    @Test
    fun `encode supports multi-tenant keys`() {
        val state = factory.default.put("tenant@vendor", "value")
        val ts = TraceStateMarshaller(state)
        assertEquals("tenant@vendor=value", ts.encode())
    }

    @Test
    fun `decode parses a single canonical list-member`() {
        val ts = TraceStateMarshaller.decode("foo=bar", factory)
        assertEquals("foo=bar", ts.encode())
    }

    @Test
    fun `decode parses multiple list-members preserving order`() {
        val ts = TraceStateMarshaller.decode("foo=1,bar=2,baz=3", factory)
        assertEquals("foo=1,bar=2,baz=3", ts.encode())
    }

    @Test
    fun `decode round-trips into encode without loss`() {
        val header = "foo=1,bar=2,baz=3"
        val ts = TraceStateMarshaller.decode(header, factory)
        assertEquals(header, ts.encode())
    }

    @Test
    fun `decode accepts multi-tenant keys`() {
        val ts = TraceStateMarshaller.decode("tenant@vendor=value", factory)
        assertEquals("tenant@vendor=value", ts.encode())
    }

    @Test
    fun `decode accepts the maximum 32 list-members`() {
        val header = (1..32).joinToString(",") { "k$it=v$it" }
        val ts = TraceStateMarshaller.decode(header, factory)
        assertEquals(header, ts.encode())
    }

    @Test
    fun `decode of an empty header returns an empty TraceState`() {
        val ts = TraceStateMarshaller.decode("", factory)
        assertEquals("", ts.encode())
    }

    @Test
    fun `decode of a header containing only commas returns an empty TraceState`() {
        val ts = TraceStateMarshaller.decode(",,,", factory)
        assertEquals("", ts.encode())
    }

    @Test
    fun `decode of a header containing only whitespace returns an empty TraceState`() {
        val ts = TraceStateMarshaller.decode("   \t  ", factory)
        assertEquals("", ts.encode())
    }

    @Test
    fun `decode trims leading and trailing OWS around list-members`() {
        val ts = TraceStateMarshaller.decode(" foo=bar ,\tbaz=qux\t", factory)
        assertEquals("foo=bar,baz=qux", ts.encode())
    }

    @Test
    fun `decode skips empty list-members between valid ones`() {
        val ts = TraceStateMarshaller.decode("foo=bar,,baz=qux", factory)
        assertEquals("foo=bar,baz=qux", ts.encode())
    }

    @Test
    fun `decode skips members lacking an equals sign and keeps the rest`() {
        val ts = TraceStateMarshaller.decode("foo=bar,nokey,baz=qux", factory)
        assertEquals("foo=bar,baz=qux", ts.encode())
    }

    @Test
    fun `decode skips members with an empty key`() {
        val ts = TraceStateMarshaller.decode("=value,foo=bar", factory)
        assertEquals("foo=bar", ts.encode())
    }

    @Test
    fun `decode skips members with an empty value`() {
        val ts = TraceStateMarshaller.decode("foo=,bar=baz", factory)
        assertEquals("bar=baz", ts.encode())
    }

    @Test
    fun `decode skips members with invalid keys and keeps the rest`() {
        // Uppercase keys are invalid per W3C spec
        val ts = TraceStateMarshaller.decode("FOO=bad,baz=qux", factory)
        assertEquals("baz=qux", ts.encode())
    }

    @Test
    fun `decode skips multi-at-sign keys and keeps the rest`() {
        val ts = TraceStateMarshaller.decode("a@b@c=bad,foo=bar", factory)
        assertEquals("foo=bar", ts.encode())
    }

    @Test
    fun `decode skips members whose value contains an equals sign`() {
        // Splitting at the first = treats the rest as the value; '=' is forbidden in values
        // so put rejects it. Other entries are kept.
        val ts = TraceStateMarshaller.decode("foo=bar=baz,ok=yes", factory)
        assertEquals("ok=yes", ts.encode())
    }

    @Test
    fun `decode keeps the first occurrence of a duplicate key and drops later ones`() {
        val ts = TraceStateMarshaller.decode("foo=first,foo=second", factory)
        assertEquals("foo=first", ts.encode())
    }

    @Test
    fun `decode silently drops list-members beyond the 32-entry cap`() {
        val header = (1..33).joinToString(",") { "k$it=v$it" }
        val expected = (1..32).joinToString(",") { "k$it=v$it" }
        val ts = TraceStateMarshaller.decode(header, factory)
        assertEquals(expected, ts.encode())
    }
}
