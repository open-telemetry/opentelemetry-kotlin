package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Compiles, links, and runs Apple tests only where they can actually run (i.e. iOS Simulator, macOS
 * host, tvOS simulator when opted in via `io.opentelemetry.kotlin.enableTvosTests`).
 */
fun Project.configureAppleTests(kotlin: KotlinMultiplatformExtension) {
    val tvosTestsEnabled = findProperty("io.opentelemetry.kotlin.enableTvosTests")?.toString()?.toBoolean() ?: false

    kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach {
        val runnable = HostManager.hostIsMac && when (konanTarget) {
            KonanTarget.IOS_SIMULATOR_ARM64 -> true
            KonanTarget.TVOS_SIMULATOR_ARM64 -> tvosTestsEnabled
            else -> false
        }
        if (!runnable) {
            val reason = "Tests for $targetName cannot run on this host or are not enabled"
            compilations.named(KotlinCompilation.TEST_COMPILATION_NAME).configure {
                compileTaskProvider.configure { onlyIf(reason) { false } }
            }
            binaries.withType(TestExecutable::class.java).configureEach {
                linkTaskProvider.configure { onlyIf(reason) { false } }
            }
            if (this is KotlinNativeTargetWithSimulatorTests) {
                val testTaskName = "${targetName}Test"
                tasks.named { it == testTaskName }.configureEach { onlyIf(reason) { false } }
            }
        }
    }
}
