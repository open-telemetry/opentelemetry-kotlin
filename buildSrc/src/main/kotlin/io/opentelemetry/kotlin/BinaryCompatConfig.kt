package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/**
 * Enables the Kotlin Gradle plugin's built-in ABI validation for modules that contain public API.
 */
@OptIn(ExperimentalAbiValidation::class)
fun Project.configureBinaryCompatValidation(kotlin: KotlinMultiplatformExtension) {
    if (!containsPublicApi()) {
        return
    }
    kotlin.abiValidation()
}
