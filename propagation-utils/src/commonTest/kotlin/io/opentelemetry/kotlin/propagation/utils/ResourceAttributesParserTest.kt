package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ResourceAttributesParserTest {

    @Test
    fun parsesResourceAttributes() {
        assertEquals(
            mapOf(
                "service.name" to "checkout",
                "encoded,key" to "value=one",
                "empty" to "",
            ),
            parseResourceAttributes(
                "service.name=first,encoded%2Ckey = value%3Done,empty=,service.name=checkout",
            ),
        )
    }

    @Test
    fun splitsOnFirstSeparator() {
        assertEquals(
            mapOf("k8s.pod.label" to "app=web", "url" to "a=b=c"),
            parseResourceAttributes("k8s.pod.label=app=web,url=a=b=c"),
        )
    }

    @Test
    fun skipsEmptyEntries() {
        assertEquals(
            mapOf("a" to "b", "c" to "d"),
            parseResourceAttributes(" ,a=b,, \t,c=d,"),
        )
        assertEquals(emptyMap(), parseResourceAttributes(""))
    }

    @Test
    fun rejectsInvalidResourceAttributes() {
        listOf(
            "missing-value",
            "missing-key=value,=value",
            "invalid%=value",
            "invalid-percent=%",
        ).forEach { value ->
            assertNull(parseResourceAttributes(value))
        }
    }
}
