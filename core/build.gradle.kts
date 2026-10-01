plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sdk-api"))
                api(project(":api-ext"))
                api(project(":noop"))
                api(project(":exporters-core"))
                api(project(":semconv"))
                implementation(project(":model"))
            }
        }
    }
}
