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
        val commonMain by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-api"))
                implementation(project(":opentelemetry-kotlin-api-ext"))
                implementation(project(":opentelemetry-kotlin-model"))
                implementation(project(":opentelemetry-kotlin-platform-implementations"))
                implementation(project(":opentelemetry-kotlin-exporters-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-test-fakes"))
                implementation(project(":opentelemetry-kotlin-integration-test"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-compat"))
                implementation(project(":opentelemetry-java-typealiases"))
                implementation(project(":opentelemetry-kotlin-integration-test"))
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
            }
        }
    }
}

tasks.register<Copy>("copyiOSTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest/resources")
}

tasks.named("iosSimulatorArm64Test").configure {
    dependsOn("copyiOSTestResources")
}
