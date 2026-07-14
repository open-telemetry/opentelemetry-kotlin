package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import net.mamoe.yamlkt.Yaml
import okio.FileSystem
import okio.Path

/**
 * Parses declarative-configuration YAML into a raw parsed value.
 *
 * Returned values follow yamlkt's representation (`Map<String, Any?>`, `List<*>`, or
 * scalars), or `null` for an empty document.
 */
@ExperimentalApi
class YamlConfigParser {

    /**
     * Parses a single YAML document from [yaml], returning the raw parsed value or `null` if
     * the document is empty.
     */
    fun parse(yaml: String): Any? {
        if (yaml.isBlank()) {
            return null
        }
        return Yaml.decodeAnyFromString(yaml)
    }

    /**
     * Reads the file at [path] from [fileSystem] and parses it as a single YAML document,
     * returning the raw parsed value or `null` if the document is empty.
     */
    fun parse(fileSystem: FileSystem, path: Path): Any? =
        parse(fileSystem.read(path) { readUtf8() })
}
