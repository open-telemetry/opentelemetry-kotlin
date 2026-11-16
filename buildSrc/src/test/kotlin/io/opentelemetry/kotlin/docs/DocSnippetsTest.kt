package io.opentelemetry.kotlin.docs

import kotlin.test.Test
import kotlin.test.assertEquals

class DocSnippetsTest {

    private val greet = """
        package example

        import example.Greeter

        // region greet
        fun greet() {
            println("hello")
        }
        // end-region
    """.trimIndent()

    private val versioned = """
        // region versioned
        val coords = "io.opentelemetry.kotlin:bom:{{version}}"
        // end-region
    """.trimIndent()

    private val errors = mutableListOf<String>()
    private val snippets = DocSnippets.parseSnippets(mapOf("Greet.kt" to greet, "Versioned.kt" to versioned), errors)

    private fun render(markdown: String, tokens: Map<String, String> = emptyMap()) =
        DocSnippets.render("doc.md", markdown, snippets, tokens, errors)

    @Test
    fun `copies lines between region tags verbatim`() {
        assertEquals(emptyList(), errors)
        assertEquals("fun greet() {\n    println(\"hello\")\n}", snippets["greet"])
    }

    @Test
    fun `replaces body of fence named by its info string`() {
        val markdown = "# Title\n\n```kotlin greet\nstale\n```\n\ntrailing text"
        val expected = "# Title\n\n```kotlin greet\nfun greet() {\n    println(\"hello\")\n}\n```\n\ntrailing text"
        assertEquals(expected, render(markdown))
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `fills an empty fence and is idempotent`() {
        val once = render("```kotlin greet\n```\n")
        assertEquals("```kotlin greet\nfun greet() {\n    println(\"hello\")\n}\n```\n", once)
        assertEquals(once, render(once))
    }

    @Test
    fun `leaves fences without an id unchanged`() {
        val markdown = "```kotlin\nval a = 1\n```\n\n```bash\n./gradlew build\n```"
        assertEquals(markdown, render(markdown))
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `substitutes tokens`() {
        val result = render("```kotlin versioned\n```", mapOf("version" to "1.2.3"))
        assertEquals("```kotlin versioned\nval coords = \"io.opentelemetry.kotlin:bom:1.2.3\"\n```", result)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `unknown token fails`() {
        render("```kotlin versioned\n```")
        assertEquals(listOf("doc.md: unknown token '{{version}}'"), errors)
    }

    @Test
    fun `unknown snippet fails and leaves markdown unchanged`() {
        val markdown = "```kotlin missing\nold\n```"
        assertEquals(markdown, render(markdown))
        assertEquals(listOf("doc.md: unknown snippet 'missing'"), errors)
    }

    @Test
    fun `duplicate snippet ids fail`() {
        val duplicateErrors = mutableListOf<String>()
        DocSnippets.parseSnippets(mapOf("A.kt" to greet, "B.kt" to greet), duplicateErrors)
        assertEquals(listOf("B.kt: duplicate snippet 'greet'"), duplicateErrors)
    }

    @Test
    fun `file without exactly one region fails`() {
        val regionErrors = mutableListOf<String>()
        DocSnippets.parseSnippets(
            mapOf(
                "None.kt" to "fun a() {}",
                "Two.kt" to "// region a\n// end-region\n// region b\n// end-region",
                "Unclosed.kt" to "// region a",
                "Reversed.kt" to "// end-region\n// region a",
            ),
            regionErrors,
        )
        val expected = "must contain exactly one '// region <id>' followed by one '// end-region'"
        assertEquals(listOf("None.kt", "Two.kt", "Unclosed.kt", "Reversed.kt").map { "$it: $expected" }, regionErrors)
    }
}
