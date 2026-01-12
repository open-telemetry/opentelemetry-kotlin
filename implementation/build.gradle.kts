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
                implementation(project(":api"))
                implementation(project(":api-ext"))
                implementation(project(":model"))
                implementation(project(":platform-implementations"))
                implementation(project(":exporters-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(project(":integration-test"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":compat"))
                implementation(project(":java-typealiases"))
                implementation(project(":integration-test"))
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
