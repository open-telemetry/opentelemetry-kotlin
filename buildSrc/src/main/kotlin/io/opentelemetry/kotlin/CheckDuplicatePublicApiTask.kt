package io.opentelemetry.kotlin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails the build if two modules declare the same public class or the same public top-level
 * function (package + name + JVM descriptor). Doing so can cause split-package clashes or
 * ambiguous calls when both modules are on the classpath.
 *
 * This is achieved by reading the JVM API dumps.
 */
abstract class CheckDuplicatePublicApiTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apiDumps: ConfigurableFileCollection

    @TaskAction
    fun check() {
        val declarations = sortedMapOf<String, MutableSet<String>>()
        apiDumps.files.forEach { dump ->
            var facade: String? = null
            dump.forEachLine { line ->
                val symbol = classRegex.find(line)?.let { match ->
                    val fqn = match.groupValues[1]
                    facade = fqn.takeIf { it.endsWith("Kt") }
                    "class $fqn"
                } ?: facade?.let { fqn ->
                    functionRegex.find(line)?.let {
                        "function ${fqn.substringBeforeLast('/')}/${it.groupValues[1]} ${it.groupValues[2]}"
                    }
                }
                symbol?.let { declarations.getOrPut(it) { sortedSetOf() }.add(dump.nameWithoutExtension) }
            }
        }

        val duplicates = declarations.filterValues { it.size > 1 }
        if (duplicates.isNotEmpty()) {
            throw GradleException(
                duplicates.entries.joinToString(
                    separator = "\n",
                    prefix = "Public API is declared in more than one module:\n",
                    postfix = "\nMove the declaration to a module both depend on, or make it a member of " +
                        "the receiver interface so each module can provide its own implementation.",
                ) { (symbol, modules) -> "  $symbol in $modules" }
            )
        }
    }

    private companion object {
        val classRegex = Regex("""^public (?:[a-z]+ )*(?:class|interface) (\S+)""")
        val functionRegex = Regex("""^\s+public static (?!synthetic)(?:[a-z]+ )*fun (\S+) (\(\S*)""")
    }
}
