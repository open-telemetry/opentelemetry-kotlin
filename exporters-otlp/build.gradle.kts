plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.buildKonfig)
}

buildkonfig {
    packageName = "io.opentelemetry.kotlin.export"

    defaultConfigs {
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "VERSION", project.version.toString())
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":exporters-protobuf"))
                implementation(project(":exporters-json"))
                implementation(project(":serializable-models"))
                implementation(project(":platform-implementations"))
                implementation(project(":platform-io"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.encoding)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
        getByName("jvmAndAndroidMain") {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        appleMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        linuxMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        linuxTest {
            dependencies {
                implementation(libs.ktor.network)
            }
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.path.contains("buildkonfig") }
}
