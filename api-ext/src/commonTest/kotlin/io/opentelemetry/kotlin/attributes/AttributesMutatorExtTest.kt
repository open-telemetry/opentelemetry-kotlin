package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AttributesMutatorExtTest {

    @Test
    fun `can set attributes with map`() {
        val mutator = FakeAttributesMutator()
        mutator.setLongAttribute("existing", 55.44.toLong())
        val map = mapOf(
            Pair("foo", "bar"), Pair("long", 21L),
            Pair("int", 123), Pair("double", 21.5), Pair("float", 22.5f),
            Pair("byte", 0x7F.toByte()), Pair("bool", true),
            Pair("bytearray", byteArrayOf(1, 2, 3)),
            Pair("list", listOf("foo", "bar", "baz")),
            Pair("tostring", TestObj("flim", 66L)),
            Pair("arrayobj", arrayOf(TestObj("one", 1), TestObj("two", 2)))
        )
        mutator.setAttributes(map)

        assertEquals("bar", mutator.attributes["foo"])
        assertEquals(21L, mutator.attributes["long"])
        assertEquals(123L, mutator.attributes[ "int"])
        assertEquals(21.5, mutator.attributes["double"])
        assertEquals(22.5, mutator.attributes["float"])
        assertEquals(127L, mutator.attributes["byte"])
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(mutator.attributes["bytearray"] as ByteArray?))
        assertEquals(listOf("foo", "bar", "baz"), mutator.attributes["list"])
        assertEquals("TestObj(first=flim, second=66)", mutator.attributes["tostring"])
        assertEquals(
            listOf("TestObj(first=one, second=1)", "TestObj(first=two, second=2)"),
            mutator.attributes["arrayobj"] as List<*>
        )
        assertEquals(55.44.toLong(), mutator.attributes["existing"])
    }

    data class TestObj(val first: String, val second: Long)
}
