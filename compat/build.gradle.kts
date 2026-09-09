plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    sourceSets {
        getByName("jvmAndAndroidMain") {
            dependencies {
                api(project(":core"))
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":config-dsl"))
                implementation(project(":model"))
                implementation(project(":java-typealiases"))
                implementation(libs.kotlinx.coroutines)
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(project.dependencies.platform(libs.opentelemetry.bom.alpha))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                implementation(libs.opentelemetry.sdk.extension.incubator)
                implementation(libs.opentelemetry.extension.trace.propagators)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(project(":integration-test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
