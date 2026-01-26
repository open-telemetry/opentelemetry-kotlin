package io.opentelemetry.kotlin

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project

private const val COMPILE_SDK_VERSION = 36
private const val MIN_SDK_VERSION = 21
fun Project.configureKotlinAndroidTarget(kotlinAndroidTarget: KotlinMultiplatformAndroidLibraryTarget) {
    kotlinAndroidTarget.apply {
        namespace = "io.opentelemetry.kotlin.${project.name.replace("-", ".")}"
        compileSdk = COMPILE_SDK_VERSION
        minSdk = MIN_SDK_VERSION

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.configureCompiler()
            }
        }
    }
}