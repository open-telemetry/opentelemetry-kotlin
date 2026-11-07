package io.opentelemetry.kotlin.config.yaml

import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlLiteral
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.YamlNull

private val PLACEHOLDER = Regex("\uE000(\\d+)\uE001")

/**
 * Matches an escape sequence or a substitution reference, scanning from left to right so that the
 * `$` an escape resolves to never starts a reference.
 */
private val ESCAPE_OR_REFERENCE = Regex("""\$\$|\$\{((?:[^}\n$]|\$(?!\$))+)\}""")
private val WHITESPACE = Regex("""\s+""")
private val NULL_VALUES = setOf("", "~", "null", "NULL")
private val ENV_SUBSTITUTION = Regex("""(?:env:)?([a-zA-Z_][a-zA-Z0-9_]*)(?::-(.*))?""")
private val BEFORE_WHOLE_VALUE = Regex("""(?:(?:^|[:-])[ \t]+|[\[{,][ \t]*)$""")
private val AFTER_WHOLE_VALUE = Regex("""^[ \t]*(?:[\]},].*|#.*)?$""")

/**
 * Substitutes environment variable references in [yaml], so that node types are interpreted
 * after substitution:
 * https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution
 *
 * Throws if a reference is invalid, or if a substituted value would change anything in the
 * document other than the scalar it was substituted into.
 */
// shortcut: yamlkt hides scalar nodes and cannot read `${` in a plain scalar, so the text is
// substituted before parsing, then checked against the document with placeholders. References in
// mapping keys fail. Needs a parser that exposes scalar nodes.
internal fun substituteEnvVars(yaml: String, getEnvVar: (String) -> String?): String {
    if ('$' !in yaml) {
        return yaml
    }
    val values = mutableListOf<String>()
    val substituted = substitute(yaml) { name, default ->
        // the platform lookup is hostile, so a failure reads as unset
        runCatching { getEnvVar(name) }.getOrNull().orEmpty().ifEmpty { default }.also { values += it }
    }
    var index = 0
    val expected = Yaml.decodeYamlFromString(substitute(yaml) { _, _ -> "\uE000${index++}\uE001" })
    require(expected.matches(Yaml.decodeYamlFromString(substituted), values)) {
        "Environment variables may only be substituted into scalar values"
    }
    return substituted
}

private fun substitute(yaml: String, resolve: (name: String, default: String) -> String): String =
    ESCAPE_OR_REFERENCE.replace(yaml) { match ->
        if (yaml.isInComment(match.range.first)) {
            return@replace match.value
        }
        val reference = match.groups[1] ?: return@replace "$"
        val envVar = requireNotNull(ENV_SUBSTITUTION.matchEntire(reference.value)) {
            "Invalid environment variable reference '${match.value}'"
        }
        resolve(envVar.groupValues[1], envVar.groupValues[2]).ifEmpty {
            // yamlkt misreads an empty plain value, so write the null it resolves to.
            "null".takeIf { yaml.isWholePlainValue(match.range) }.orEmpty()
        }
    }

private fun String.isWholePlainValue(range: IntRange): Boolean {
    val before = substring(lastIndexOf('\n', range.first) + 1, range.first)
    val after = substring(range.last + 1, indexOf('\n', range.last).takeIf { it >= 0 } ?: length)
    return BEFORE_WHOLE_VALUE.containsMatchIn(before) && AFTER_WHOLE_VALUE.matches(after)
}

// shortcut: quotes are tracked within one line, so a quoted scalar spanning lines can hide a
// comment. Needs a parser that exposes comments.
private fun String.isInComment(index: Int): Boolean {
    val lineStart = lastIndexOf('\n', index - 1) + 1
    var quote: Char? = null
    var i = lineStart
    while (i < index) {
        val char = this[i]
        val startsToken = i == lineStart || this[i - 1].isWhitespace() || this[i - 1] in "[{,"
        when {
            quote == '"' && char == '\\' -> i++
            quote != null -> quote = quote.takeUnless { char == it }
            char == '#' && startsToken -> return true
            (char == '"' || char == '\'') && startsToken -> quote = char
        }
        i++
    }
    return false
}

/**
 * Whether [actual] has the same keys, nesting and scalars as this document once each [PLACEHOLDER]
 * is replaced by the value it stands for.
 */
private fun YamlElement.matches(actual: YamlElement, values: List<String>): Boolean = when (this) {
    is YamlMap ->
        actual is YamlMap && content.keys == actual.content.keys &&
            content.all { (key, value) -> value.matches(actual.content.getValue(key), values) }
    is YamlList ->
        actual is YamlList && content.size == actual.content.size &&
            content.zip(actual.content).all { (value, actualValue) -> value.matches(actualValue, values) }
    is YamlLiteral -> content.replace(PLACEHOLDER) { values[it.groupValues[1].toInt()] }.matches(actual)
    YamlNull -> actual == YamlNull
}

private fun String.matches(actual: YamlElement): Boolean = when (actual) {
    is YamlLiteral -> folded() == actual.content.folded()
    YamlNull -> folded() in NULL_VALUES
    else -> false
}

// YAML folds line breaks in a flow scalar and trims it, which cannot change the structure.
private fun String.folded(): String = replace(WHITESPACE, " ").trim()
