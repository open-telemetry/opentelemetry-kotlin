package io.opentelemetry.kotlin.config.yaml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Cases from the table in
 * https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution
 *
 * yamlkt cannot read `{`, `}` or `:` in an unquoted scalar, so cases that leave them in the value
 * are quoted.
 */
internal class EnvVarSubstitutionTest {

    private val envVars = mapOf(
        "STRING_VALUE" to "value",
        "BOOL_VALUE" to "true",
        "INT_VALUE" to "1",
        "FLOAT_VALUE" to "1.1",
        "INVALID_MAP_VALUE" to "value\nkey: value",
        "INVALID_LIST_VALUE" to "value\n  - value",
        "QUOTE_BREAKING_VALUE" to "value\", key: \"value",
        "DO_NOT_REPLACE_ME" to "Never use this value",
        "REPLACE_ME" to "\${DO_NOT_REPLACE_ME}",
        "VALUE_WITH_ESCAPE" to "value\$\$",
        "EMPTY_VALUE" to "",
        "WHITESPACE_VALUE" to " ",
    )
    private val parser = YamlConfigParser(envVars::get)

    @Test
    fun substitutesScalarValues() {
        val cases = listOf(
            "\${STRING_VALUE}" to "value",
            "\${BOOL_VALUE}" to true,
            "\${INT_VALUE}" to 1,
            "\${FLOAT_VALUE}" to 1.1,
            "\"\${STRING_VALUE}\"" to "value",
            "\"\${BOOL_VALUE}\"" to "true",
            "\"\${INT_VALUE}\"" to "1",
            "\"\${FLOAT_VALUE}\"" to "1.1",
            "\${env:STRING_VALUE}" to "value",
            "foo \${STRING_VALUE} \${FLOAT_VALUE}" to "foo value 1.1",
            "a \${STRING_VALUE}\n  b" to "a value b",
            "\${UNDEFINED_KEY}" to null,
            "\"\${UNDEFINED_KEY}\"" to "",
            "\${UNDEFINED_KEY:-fallback}" to "fallback",
            "\${EMPTY_VALUE:-fallback}" to "fallback",
            "\"\${REPLACE_ME}\"" to "\${DO_NOT_REPLACE_ME}",
            "\"\${UNDEFINED_KEY:-\${STRING_VALUE}}\"" to "\${STRING_VALUE}",
            "\"\$\${STRING_VALUE}\"" to "\${STRING_VALUE}",
            "\$\$\${STRING_VALUE}" to "\$value",
            "\"\$\$\$\${STRING_VALUE}\"" to "\$\${STRING_VALUE}",
            "\"\$\${STRING_VALUE:-fallback}\"" to "\${STRING_VALUE:-fallback}",
            "\"\$\${STRING_VALUE:-\${STRING_VALUE}}\"" to "\${STRING_VALUE:-value}",
            "\"\${UNDEFINED_KEY:-\$\${UNDEFINED_KEY}}\"" to "\${UNDEFINED_KEY:-\${UNDEFINED_KEY}}",
            "\${VALUE_WITH_ESCAPE}" to "value\$\$",
            "a \$\$ b" to "a \$ b",
            "a \$ b" to "a \$ b",
            "\"\${}\"" to "\${}",
        )

        cases.forEach { (input, expected) ->
            assertEquals(mapOf("key" to expected), parser.parse("key: $input"), "substituting <$input>")
        }
    }

    @Test
    fun substitutesSequenceElements() {
        val yaml = "list:\n  - \${INT_VALUE}\n  - \${STRING_VALUE}"
        assertEquals(mapOf("list" to listOf(1, "value")), parser.parse(yaml))
    }

    @Test
    fun keepsUndefinedFlowMappingValue() {
        val yaml = "map: {key: \${UNDEFINED_KEY}, other: \${STRING_VALUE}}"
        assertEquals(mapOf("map" to mapOf("key" to null, "other" to "value")), parser.parse(yaml))
    }

    @Test
    fun ignoresReferencesInComments() {
        val yaml = "# \${STRING_VALUE:?required}\nkey: \"#\${STRING_VALUE}\" # example: \${1API_KEY}"
        assertEquals(mapOf("key" to "#value"), parser.parse(yaml))
    }

    @Test
    fun keepsReferencesInMappingKeys() {
        val yaml = "map: {\"\${STRING_VALUE}\": \"\${STRING_VALUE}\", \"\$\$\${1API_KEY}\": 1}"
        val expected = mapOf("\${STRING_VALUE}" to "value", "\$\$\${1API_KEY}" to 1)
        assertEquals(mapOf("map" to expected), parser.parse(yaml))
    }

    @Test
    fun undefinedReferenceIsNullOnlyAsWholePlainScalar() {
        assertEquals(mapOf("key" to "a\n\nb\n"), parser.parse("key: |\n  a\n  \${UNDEFINED_KEY}\n  b\n"))
        assertEquals(
            parser.parse("key: \"a\n  \n  b\""),
            parser.parse("key: \"a\n  \${UNDEFINED_KEY}\n  b\""),
        )
    }

    @Test
    fun substitutesAfterHashInScalars() {
        val cases = listOf(
            "'it''s # \${STRING_VALUE}'" to "it's # value",
            "\"a\n  # \${STRING_VALUE}\"" to "a # value",
            "|\n  # \${STRING_VALUE}\n" to "# value\n",
        )

        cases.forEach { (input, expected) ->
            assertEquals(mapOf("key" to expected), parser.parse("key: $input"), "substituting <$input>")
        }
    }

    @Test
    fun decodesQuotedDefaultLikeTheScalar() {
        assertEquals(mapOf("key" to "a\tb"), parser.parse("key: \"\${UNDEFINED_KEY:-a\\tb}\""))
    }

    @Test
    fun keepsWhitespaceValue() {
        assertEquals(mapOf("key" to " "), parser.parse("key: \"\${WHITESPACE_VALUE:-fallback}\""))
    }

    @Test
    fun valueCannotInjectStructure() {
        val documents = listOf(
            "key: \${INVALID_MAP_VALUE}",
            "other: \${INVALID_MAP_VALUE}",
            "other: \${INVALID_MAP_VALUE}\nkey: 1",
            "list:\n  - \${INVALID_LIST_VALUE}",
            "{key: \"\${QUOTE_BREAKING_VALUE}\"}",
        )

        documents.forEach { yaml ->
            assertFailsWith<IllegalArgumentException>("parsing <$yaml> should fail") {
                parser.parse(yaml)
            }
        }
    }

    @Test
    fun quotedValueWithStructureStaysScalar() {
        val result = parser.parse("key: \"\${INVALID_MAP_VALUE}\"") as Map<*, *>

        assertEquals(setOf("key"), result.keys)
        assertEquals(String::class, result["key"]!!::class)
    }

    @Test
    fun invalidReferenceFails() {
        val references = listOf("\${STRING_VALUE:?error}", "\${1API_KEY}", "\${API_\$KEY}", "\${sys:STRING_VALUE}")

        references.forEach { reference ->
            assertFailsWith<IllegalArgumentException>("parsing <$reference> should fail") {
                parser.parse("key: \"$reference\"")
            }
        }
    }
}
