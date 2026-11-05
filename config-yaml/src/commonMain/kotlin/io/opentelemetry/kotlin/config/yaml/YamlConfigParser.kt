package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.getEnvVarValue
import net.mamoe.yamlkt.Yaml
import okio.FileSystem
import okio.Path

/**
 * Parses declarative-configuration YAML into a raw parsed value.
 *
 * Returned values follow yamlkt's representation (`Map<String, Any?>`, `List<*>`, or
 * scalars), or `null` for an empty document. Environment variable references in scalar values are
 * substituted from [envVarReader].
 */
@ExperimentalApi
class YamlConfigParser(
    private val envVarReader: EnvVarReader = EnvVarReader(::getEnvVarValue),
) {

    /**
     * Parses a single YAML document from [yaml], returning the raw parsed value or `null` if
     * the document is empty.
     */
    fun parse(yaml: String): Any? {
        if (yaml.isBlank()) {
            return null
        }
        return Yaml.decodeAnyFromString(substituteEnvVars(yaml, envVarReader))
    }

    /**
     * Reads the file at [path] from [fileSystem] and parses it as a single YAML document,
     * returning the raw parsed value or `null` if the document is empty.
     */
    fun parse(fileSystem: FileSystem, path: Path): Any? =
        parse(fileSystem.read(path) { readUtf8() })
}
