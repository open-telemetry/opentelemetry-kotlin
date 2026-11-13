import kotlinx.benchmark.gradle.benchmark
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlinx.benchmark)
}

configure<AllOpenExtension> {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    jvmToolchain(11)
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":implementation"))
                implementation(project(":sdk-common"))
                implementation(project(":benchmark-fixtures"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                implementation(project(":compat"))
                implementation(project(":implementation"))
                implementation(project(":benchmark-fixtures"))
                implementation(project(":java-typealiases"))
            }
        }
    }
    compilerOptions.optIn.add("io.opentelemetry.kotlin.ExperimentalApi")
}

benchmark {
    targets {
        register("jvm")
    }
    configurations {
        // trade off between accuracy & speed
        register("perf") {
            warmups = 1
            iterations = 5
            mode = "avgt"
            outputTimeUnit = "ns"
            iterationTime = 1
            iterationTimeUnit = "s"
        }
    }
}
