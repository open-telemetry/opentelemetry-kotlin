package io.opentelemetry.kotlin

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project


fun Project.configureKotlinAndroidTarget(kotlinAndroidTarget: KotlinMultiplatformAndroidLibraryTarget) {
    kotlinAndroidTarget.apply {
        namespace = "io.opentelemetry.kotlin.${project.name.replace("-", ".")}"
        compileSdk = 36
        minSdk = 21

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.configureCompiler()
            }
        }
    }
}