package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

private val KOTLIN_VERSION = KotlinVersion.KOTLIN_2_0
private const val JDK_VERSION = 11

fun Project.configureKotlin(
    kotlin: KotlinMultiplatformExtension
) {
    kotlin.apply {
        jvmToolchain(JDK_VERSION)
        compilerOptions.configureCompiler()
        jvm {
            compilerOptions.configureCompiler()
        }

        if (!project.isJvmAndroidModule()) {
            js(IR) {
                nodejs()
                browser {
                    testTask {
                        // disable browser tests, as nodejs is enough
                        enabled = false
                    }
                }
                binaries.library()
            }

            val frameworkName = createIosFrameworkName(project.name)
            val framework = XCFramework(frameworkName)

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { target ->
                compilerOptions.configureCompiler()
                target.binaries.framework {
                    baseName = frameworkName
                }
                framework.add(target.binaries.getFramework("RELEASE"))
            }
        }

        sourceSets.apply {
            applyDefaultHierarchyTemplate()

            getByName("commonMain").apply {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION.version}.0")
                    // add dependencies here
                }
            }
            getByName("commonTest").apply {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test") {
                        exclude(group = "junit")
                        exclude(group = "org.junit")
                    }
                }
            }

            val jvmAndAndroidMain = create("jvmAndAndroidMain").apply {
                dependsOn(commonMain.get())
            }
            getByName("androidMain").apply {
                dependsOn(jvmAndAndroidMain)
            }
            getByName("jvmMain").apply {
                dependsOn(jvmAndAndroidMain)
            }

            if (!project.isJvmAndroidModule()) {
                getByName("jsMain")
            }
        }
        compilerOptions {
            configureCompiler()
        }
    }
}

fun KotlinCommonCompilerOptions.configureCompiler() {
    allWarningsAsErrors.set(true)
    apiVersion.set(KOTLIN_VERSION)
    languageVersion.set(KOTLIN_VERSION)
    freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xsuppress-version-warnings")
}

private fun createIosFrameworkName(input: String): String {
    return input
        .split("-")
        .mapIndexed { _, part ->
            val normalized = when {
                part.equals("opentelemetry") -> "Otel"
                else -> part.replaceFirstChar { it.uppercase() }
            }
            normalized
        }
        .joinToString("")
}
