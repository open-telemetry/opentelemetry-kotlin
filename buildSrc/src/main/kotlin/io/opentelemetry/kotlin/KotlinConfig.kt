package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
            configureNonJvmTargets()
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
                // shared by every target except JS
                val nonJsMain = create("nonJsMain").apply {
                    dependsOn(commonMain.get())
                }
                jvmAndAndroidMain.dependsOn(nonJsMain)
                getByName("nativeMain").apply {
                    dependsOn(nonJsMain)
                }
            }
        }
        compilerOptions {
            configureCompiler()
        }
    }
}

/**
 * Declares every target other than JVM and Android. This is the single place to add a new target.
 */
private fun KotlinMultiplatformExtension.configureNonJvmTargets() {
    js {
        nodejs()
        browser {
            testTask {
                // disable browser tests, as nodejs is enough
                enabled = false
            }
        }
    }

    // assume that consumers depend on the published klibs and link their own frameworks
    iosArm64()
    iosSimulatorArm64()
    tvosArm64()
    tvosSimulatorArm64()

    linuxX64()
    linuxArm64()
}

fun KotlinCommonCompilerOptions.configureCompiler() {
    allWarningsAsErrors.set(true)
    apiVersion.set(KOTLIN_VERSION)
    languageVersion.set(KOTLIN_VERSION)
    freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xsuppress-version-warnings")
}
