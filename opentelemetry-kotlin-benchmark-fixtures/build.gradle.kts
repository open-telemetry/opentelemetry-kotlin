plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":opentelemetry-kotlin-api"))
                implementation(project(":opentelemetry-kotlin-api-ext"))
                implementation(project(":opentelemetry-kotlin-noop"))
                implementation(project(":opentelemetry-kotlin-implementation"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                implementation(project(":opentelemetry-kotlin-api"))
                implementation(project(":opentelemetry-kotlin-api-ext"))
                implementation(project(":opentelemetry-kotlin-noop"))
                implementation(project(":opentelemetry-kotlin-compat"))
                implementation(project(":opentelemetry-kotlin-implementation"))
                implementation(project(":opentelemetry-java-typealiases"))
            }
        }
    }
}
