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
        getByName("commonMain") {
            dependencies {
                implementation(project(":sdk-api"))
                api(project(":behavior"))
                api(project(":config-schema"))
                implementation(project(":platform-implementations"))
                implementation(libs.yamlkt)
                implementation(libs.okio)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlin.test)
                implementation(libs.okio.fakefilesystem)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.kotlin.test)
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
