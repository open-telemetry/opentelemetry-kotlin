package io.opentelemetry.kotlin.integration.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.PrintStream
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class MinSupportedVersionsTest {

    @Test
    fun `min supported versions can consume jvm and android artifacts`(@TempDir tmp: Path) {
        assembleFixture(
            fixtureName = "min-versions-android-app",
            gradleVersion = systemProperty("minSupportedGradle"),
            tmp = tmp,
        )
    }

    // Klib (Apple/JS) consumers need at least the Kotlin version this library is built with, so the
    // fixture builds with the declared klib floor (minSupportedKotlinKlib). A Kotlin bump that
    // outgrows that pin fails here — raising the floor must be a deliberate decision, not a side
    // effect of a dependency update. Apple platform coverage requires a macOS host.
    @Test
    fun `min supported versions can consume klib artifacts`(@TempDir tmp: Path) {
        assembleFixture(
            fixtureName = "min-versions-kmp-app",
            gradleVersion = systemProperty("minSupportedGradle"),
            tmp = tmp,
        )
    }

    private fun assembleFixture(
        fixtureName: String,
        gradleVersion: String,
        tmp: Path,
    ) {
        val fixtureSrc = File(systemProperty("fixtureSrcDir"), fixtureName).toPath()
        copyRecursively(fixtureSrc, tmp)

        val runner = GradleRunner.create()
            .withTestKitDir(File(systemProperty("testKitDir")))
            .withProjectDir(tmp.toFile())
            .withGradleVersion(gradleVersion)
            .withArguments(
                "-PcatalogPath=${systemProperty("catalogPath")}",
                "-PotelKotlinVersion=${systemProperty("otelKotlinVersion")}",
                "-PjvmAndroidModules=${systemProperty("jvmAndroidModules")}",
                "-PklibModules=${systemProperty("klibModules")}",
                "assemble",
                "--info",
                "--stacktrace",
                "--no-configuration-cache",
            )
            .forwardStdOutput(LinePrefixingWriter("[$fixtureName] ", System.out))
            .forwardStdError(LinePrefixingWriter("[$fixtureName] ", System.err))
        runner.build()
    }

    private class LinePrefixingWriter(
        private val prefix: String,
        private val out: PrintStream,
    ) : Writer() {
        private val line = StringBuilder()

        override fun write(cbuf: CharArray, off: Int, len: Int) {
            for (i in off until off + len) {
                val c = cbuf[i]
                if (c == '\n') {
                    flushLine()
                } else {
                    line.append(c)
                }
            }
        }

        override fun flush() {
            out.flush()
        }

        override fun close() {
            if (line.isNotEmpty()) {
                flushLine()
            }
            flush()
        }

        private fun flushLine() {
            out.println(prefix + line)
            line.setLength(0)
        }
    }

    private fun systemProperty(name: String): String =
        requireNotNull(System.getProperty(name)) { "system property '$name' was not set by the Gradle test task" }

    private fun copyRecursively(source: Path, target: Path) {
        Files.walk(source).use { stream ->
            stream.forEach { path ->
                val rel = source.relativize(path)
                val dest = target.resolve(rel.toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest)
                } else {
                    Files.createDirectories(dest.parent)
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}
