package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family

/**
 * Copies `src/commonTest/resources` next to every native test executable, where test fixture
 * loaders expect to find it. Linux tests also run from that directory, as they resolve fixtures
 * relative to the working directory.
 */
fun Project.configureNativeTests(kotlin: KotlinMultiplatformExtension) {
    kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach {
        val targetName = name
        val capitalizedName = targetName.replaceFirstChar { it.uppercase() }
        val testDirectory = layout.buildDirectory.dir("bin/$targetName/debugTest")
        val copyResources = tasks.register("copy${capitalizedName}TestResources", Copy::class.java) {
            from(layout.projectDirectory.dir("src/commonTest/resources"))
            into(testDirectory.map { it.dir("resources") })
        }
        tasks.named("linkDebugTest$capitalizedName").configure {
            dependsOn(copyResources)
        }
        if (konanTarget.family == Family.LINUX) {
            tasks.withType(KotlinNativeTest::class.java).configureEach {
                if (name == "${targetName}Test") {
                    workingDir = testDirectory.get().asFile.absolutePath
                }
            }
        }
    }
}
