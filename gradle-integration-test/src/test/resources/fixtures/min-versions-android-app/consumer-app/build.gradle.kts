import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

// A com.android.application consumer so DEX runs against the artifacts at the minimum
// supported AGP / Gradle / minSdk — coverage the android-library sibling doesn't exercise.
//
// AGP and the Kotlin plugin are already on the build classpath via the root project
// (com.android.library and kotlin.multiplatform share the same jars), so they are applied here
// without a version — requesting one in a subproject fails with "already on the classpath".
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val otelKotlinVersion: String = providers.gradleProperty("otelKotlinVersion").get()
val fixtureKotlinLang = KotlinVersion.fromVersion(libs.versions.minSupportedKotlinLang.get())

android {
    namespace = "io.opentelemetry.kotlin.minversion.app"
    compileSdk = libs.versions.android.minCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "io.opentelemetry.kotlin.minversion.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        // This fixture exists to verify compilation + dexing at the minimum toolchain. On an
        // application module `assemble` also runs lintVitalRelease, which OOMs the CI worker's
        // Metaspace and adds nothing to what this test checks.
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        apiVersion.set(fixtureKotlinLang)
        languageVersion.set(fixtureKotlinLang)
    }
}

dependencies {
    implementation("io.opentelemetry.kotlin:api:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:sdk-api:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:core:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:implementation:$otelKotlinVersion")
    implementation("io.opentelemetry.kotlin:exporters-core:$otelKotlinVersion")
}
