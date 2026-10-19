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
    fun rejectsInvalidResourceAttributes() {
        listOf(
            "",
            "missing-value",
            "missing-key=value,=value",
            "too=many=separators",
            "invalid%=value",
            "invalid-percent=%",
        ).forEach { value ->
            assertNull(parseResourceAttributes(value))
        }
    }
}
