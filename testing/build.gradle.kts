plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvmToolchain(17)
    sourceSets {
        val jvmAndAndroidMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":compat"))
                api(project(":java-typealiases"))

                api(project.dependencies.platform(libs.opentelemetry.bom))
                api(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                compileOnly(libs.bundles.junit)
            }
        }
    }
}
