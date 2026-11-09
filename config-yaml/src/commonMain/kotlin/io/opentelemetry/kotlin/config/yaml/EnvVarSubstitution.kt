package io.opentelemetry.kotlin.config.yaml

import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlLiteral
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.YamlNull

/**
 * Matches an escape sequence or a substitution reference, scanning from left to right so that the
 * `$` an escape resolves to never starts a reference.
 */
private val ESCAPE_OR_REFERENCE = Regex("""\$\$|\$\{((?:[^}\n$]|\$(?!\$))+)\}""")

/** A marked match as YAML decodes it: its index, the decoded content between the braces, a probe. */
private val MARKER = Regex("\uE000(\\d+)\uE001([^\uE002\uE003]*)(?:\uE002|\uE003 ?)")
private const val HIDDEN = 0xF000
private const val ASCII = 128
private val WHITESPACE = Regex("""\s+""")
private val NULL_VALUES = setOf("", "~", "null", "NULL")
private val ENV_SUBSTITUTION = Regex("""(?:env:)?([a-zA-Z_][a-zA-Z0-9_]*)(?::-(.*))?""")

/**
 * Substitutes environment variable references in the scalar values of [yaml], so that node types
 * are interpreted after substitution:
 * https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution
 *
 * Throws if a reference is invalid, or if a substituted value would change anything in the
 * document other than the scalar it was substituted into.
 */
// shortcut: yamlkt hides scalar nodes and cannot read `${` in a plain scalar. Each match is replaced
// by a marker, and the parsed marker document tells which matches sit in values, keys or comments.
// The values are then substituted into the text, so a reference alone on a line of a multi-line
// flow scalar leaves an empty line, and an unset reference that is a whole `|-` block reads as null.
// Needs a parser that exposes scalar nodes.
internal fun substituteEnvVars(yaml: String, getEnvVar: (String) -> String?): String {
    val matches = ESCAPE_OR_REFERENCE.findAll(yaml).toList()
    if (matches.isEmpty()) {
        return yaml
    }
    val marked = yaml.replace(matches) { index, match ->
        // Only a plain scalar trims this probe, so a plain scalar that is just the marker is whole. It is
        // left out where YAML would strip it or read a comment, so a probe is always the space it adds.
        val end = match.range.last + 1
        val lineEnd = yaml.indexOf('\n', end).takeIf { it >= 0 } ?: yaml.length
        val atLineEnd = yaml.getOrNull(end) == '#' || yaml.substring(end, lineEnd).isBlank()
        val probe = when {
            atLineEnd -> "\uE002"
            else -> "\uE003 "
        }
        "\uE000$index\uE001${match.groupValues[1].hidePunctuation()}$probe"
    }
    val expected = Yaml.decodeYamlFromString(marked)
    val values = mutableMapOf<Int, String>()
    val texts = mutableMapOf<Int, String>()
    expected.forEachValue { scalar ->
        MARKER.findAll(scalar).forEach { marker ->
            val index = marker.groupValues[1].toInt()
            val match = matches[index]
            if (match.value == "$$") {
                values[index] = "$"
                texts[index] = "$"
                return@forEach
            }
            val envVar = requireNotNull(ENV_SUBSTITUTION.matchEntire(marker.groupValues[2].showPunctuation())) {
                "Invalid environment variable reference '${match.value}'"
            }
            // the platform lookup is hostile, so a failure reads as unset
            val value = runCatching { getEnvVar(envVar.groupValues[1]) }.getOrNull().orEmpty()
            values[index] = value.ifEmpty { envVar.groupValues[2] }
            // the raw default decodes in place to the default the marker document decoded
            texts[index] = value.ifEmpty { match.groupValues[1].substringAfter(":-", "") }.ifEmpty {
                // yamlkt misreads an empty plain value, so write the null it resolves to.
                "null".takeIf { marker.value == scalar && !scalar.endsWith(' ') }.orEmpty()
            }
        }
    }
    val substituted = yaml.replace(matches) { index, match -> texts[index] ?: match.value }
    require(expected.matches(Yaml.decodeYamlFromString(substituted), matches, values)) {
        "Environment variables may only be substituted into scalar values"
    }
    return substituted
}

private fun String.replace(matches: List<MatchResult>, transform: (Int, MatchResult) -> String): String =
    buildString {
        var end = 0
        matches.forEachIndexed { index, match ->
            append(this@replace, end, match.range.first).append(transform(index, match))
            end = match.range.last + 1
        }
        append(this@replace, end, this@replace.length)
    }

// yamlkt cannot read punctuation such as `:` or `{` in a plain scalar, so it is moved out of ASCII.
// Backslashes and quotes stay, so a quoted scalar still decodes its escapes.
private fun String.hidePunctuation(): String =
    map {
        when {
            it.code < ASCII && !it.isLetterOrDigit() && it !in " _\\'\"" -> it + HIDDEN
            else -> it
        }
    }.joinToString("")

private fun String.showPunctuation(): String =
    map {
        when (it.code) {
            in HIDDEN until HIDDEN + ASCII -> it - HIDDEN
            else -> it
        }
    }.joinToString("")

private fun YamlElement.forEachValue(action: (String) -> Unit) {
    when (this) {
        is YamlMap -> content.values.forEach { it.forEachValue(action) }
        is YamlList -> content.forEach { it.forEachValue(action) }
        is YamlLiteral -> action(content)
        YamlNull -> Unit
    }
}

/**
 * Whether [actual] has the same keys, nesting and scalars as this marker document once each
 * marker in a key is restored to its match and each marker in a value is replaced by its value.
 */
private fun YamlElement.matches(actual: YamlElement, matches: List<MatchResult>, values: Map<Int, String>): Boolean =
    when (this) {
        is YamlMap -> content.mapKeys { (key) -> key.restoreKey(matches) }.let { restored ->
            actual is YamlMap && restored.keys == actual.content.keys &&
                restored.all { (key, value) -> value.matches(actual.content.getValue(key), matches, values) }
        }
        is YamlList ->
            actual is YamlList && content.size == actual.content.size &&
                content.zip(actual.content).all { (value, actualValue) -> value.matches(actualValue, matches, values) }
        is YamlLiteral -> content.replace(MARKER) { values.getValue(it.groupValues[1].toInt()) }.matches(actual)
        YamlNull -> actual == YamlNull
    }

// Mapping keys are not substituted, so each marker in a key is restored to the match it replaced.
private fun YamlElement.restoreKey(matches: List<MatchResult>): YamlElement = when (this) {
    is YamlLiteral -> YamlLiteral(
        content.replace(MARKER) {
            when (matches[it.groupValues[1].toInt()].value) {
                "$$" -> "$$"
                else -> "\${${it.groupValues[2].showPunctuation()}}"
            }
        }
    )
    else -> this
}

private fun String.matches(actual: YamlElement): Boolean = when (actual) {
    is YamlLiteral -> folded() == actual.content.folded()
    YamlNull -> folded() in NULL_VALUES
    else -> false
}

// YAML folds line breaks in a flow scalar and trims it, which cannot change the structure.
private fun String.folded(): String = replace(WHITESPACE, " ").trim()
