import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.buildKonfig)
}

buildkonfig {
    packageName = "io.opentelemetry.kotlin"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SDK_VERSION", project.version.toString())
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":api-ext"))
                implementation(project(":sdk-api"))
                implementation(project(":model"))
                implementation(project(":platform-implementations"))
                implementation(project(":semconv"))
                implementation(project(":exporters-core"))
                implementation(project(":noop"))
                implementation(project(":semconv"))
                implementation(libs.kotlinx.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":api-ext"))
                implementation(project(":test-fakes"))
                implementation(project(":integration-test"))
                implementation(libs.kotlinx.coroutines.test)
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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.path.contains("buildkonfig") }
}

tasks.register<Copy>("copyiOSTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest/resources")
}

tasks.named("iosSimulatorArm64Test").configure {
    dependsOn("copyiOSTestResources")
}
