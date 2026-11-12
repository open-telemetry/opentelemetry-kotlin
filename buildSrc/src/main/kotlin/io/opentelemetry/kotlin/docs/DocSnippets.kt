package io.opentelemetry.kotlin.docs

/**
 * Copies snippets from source files into markdown code fences.
 *
 * Each source file holds exactly one snippet: the lines between `// region <id>` and
 * `// end-region`. A markdown fence whose info string names that id after the language, e.g.
 * ```` ```kotlin <id> ````, has its body replaced with the snippet verbatim.
 */
internal object DocSnippets {

    private val regionStart = Regex("""^// region (\S+)$""")
    private const val REGION_END = "// end-region"
    private val fence = Regex(
        """^(```\w+ (\S+)\n)(?:.*?\n)?```$""",
        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL),
    )
    private val token = Regex("""\{\{(\w+)}}""")

    /**
     * Returns the snippet in each of [sources] (source name to file text), keyed by snippet id.
     */
    fun parseSnippets(sources: Map<String, String>, errors: MutableList<String>): Map<String, String> {
        val snippets = mutableMapOf<String, String>()
        sources.forEach { (source, text) ->
            val lines = text.lines()
            val start = lines.indices.singleOrNull { regionStart.matches(lines[it]) }
            val end = lines.indices.singleOrNull { lines[it] == REGION_END }
            if (start == null || end == null || end < start) {
                errors += "$source: must contain exactly one '// region <id>' followed by one '$REGION_END'"
                return@forEach
            }
            val id = regionStart.matchEntire(lines[start])!!.groupValues[1]
            if (snippets.put(id, lines.subList(start + 1, end).joinToString("\n")) != null) {
                errors += "$source: duplicate snippet '$id'"
            }
        }
        return snippets
    }

    /**
     * Returns [markdown] with each snippet fence's body replaced by that snippet, after
     * substituting `{{token}}` values from [tokens].
     */
    fun render(
        fileName: String,
        markdown: String,
        snippets: Map<String, String>,
        tokens: Map<String, String>,
        errors: MutableList<String>,
    ): String = fence.replace(markdown) { match ->
        val (opening, id) = match.destructured
        val snippet = snippets[id]
        if (snippet == null) {
            errors += "$fileName: unknown snippet '$id'"
            return@replace match.value
        }
        val body = token.replace(snippet) { tokenMatch ->
            tokens[tokenMatch.groupValues[1]] ?: tokenMatch.value.also {
                errors += "$fileName: unknown token '$it'"
            }
        }
        "$opening$body\n```"
    }
}
