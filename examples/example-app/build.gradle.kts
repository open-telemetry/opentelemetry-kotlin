import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(11)

    androidLibrary {
        namespace = "io.opentelemetry.kotlin.example.app"
        compileSdk = 36
        minSdk = 21
    }

    jvm()

    js(IR) {
        nodejs()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "SharedExampleApp"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":implementation"))
                implementation(project(":semconv"))
                implementation(libs.kotlinx.coroutines)
                implementation(project(":exporters-otlp"))
            }
        }

        val composeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }

        val androidMain by getting {
            dependsOn(composeMain)
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val jvmMain by getting {
            dependsOn(composeMain)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(composeMain)
        }

        val jsMain by getting
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xsuppress-version-warnings")
    }
}

composeCompiler {
    targetKotlinPlatforms.set(
        KotlinPlatformType.entries
            .filter { it != KotlinPlatformType.js }
            .asIterable()
    )
}

compose.desktop {
    application {
        mainClass = "io.opentelemetry.example.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "example-app"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runConsoleExampleApp") {
    dependsOn("jvmMainClasses")
    val compilation = kotlin.targets.named("jvm").get().compilations.named("main").get()
    classpath(
        compilation.output.allOutputs,
        compilation.runtimeDependencyFiles
    )
    mainClass.set("io.opentelemetry.example.app.console.MainKt")
}

// Convenience tasks for running platform-specific example apps
tasks.register("runJvmExampleApp") {
    dependsOn("run")
}

tasks.register("runNodeExampleApp") {
    dependsOn("jsNodeDevelopmentRun")
}

tasks.register("runAndroidExampleApp") {
    dependsOn(":examples:example-app-android:installDebug")
    doLast {
        println("Android app installed. Please launch it manually from your device/emulator.")
    }
}

tasks.register<Exec>("runIosExampleApp") {
    workingDir = file(".")
    commandLine("sh", "-c", "./run-ios.sh")
}
