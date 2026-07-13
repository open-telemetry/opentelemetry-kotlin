package io.opentelemetry.kotlin.integration.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
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

    // Klib (iOS/JS) consumers need at least the Kotlin version this library is built with, so the
    // fixture builds with the declared klib floor (minSupportedKotlinKlib). A Kotlin bump that
    // outgrows that pin fails here — raising the floor must be a deliberate decision, not a side
    // effect of a dependency update. iOS coverage requires a macOS host.
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
            .withProjectDir(tmp.toFile())
            .withGradleVersion(gradleVersion)
            .withArguments(
                "-PcatalogPath=${systemProperty("catalogPath")}",
                "-PotelKotlinVersion=${systemProperty("otelKotlinVersion")}",
                "assemble",
                "--info",
                "--stacktrace",
                "--no-configuration-cache",
            )
            .forwardOutput()
        runner.build()
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
