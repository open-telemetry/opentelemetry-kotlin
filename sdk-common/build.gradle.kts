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
    exposeObjectWithName = "SdkBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SDK_VERSION", project.version.toString())
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":sdk-api"))
                implementation(project(":platform-implementations"))
                implementation(project(":semconv"))
                implementation(libs.kotlinx.coroutines)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(project(":test-fakes"))
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.path.contains("buildkonfig") }
}
