import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.test.kotlin.multiplatform)
    alias(libs.plugins.test.android.library)
}

val otelKotlinVersion: String = providers.gradleProperty("otelKotlinVersion").get()
val fixtureKotlinLang = KotlinVersion.fromVersion(libs.versions.minSupportedKotlinLang.get())

android {
    namespace = "io.opentelemetry.kotlin.minversion"
    compileSdk = libs.versions.android.minCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compilerOptions {
        apiVersion.set(fixtureKotlinLang)
        languageVersion.set(fixtureKotlinLang)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.opentelemetry.kotlin:api:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:sdk-api:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:core:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:implementation:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:exporters-core:$otelKotlinVersion")
            }
        }
    }
}
