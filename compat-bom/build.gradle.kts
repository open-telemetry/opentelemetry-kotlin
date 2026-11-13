plugins {
    id("java-platform")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("io.opentelemetry.kotlin.build-logic")
}

description = "OpenTelemetry Kotlin Java Compatibility BOM"

javaPlatform {
    allowDependencies()
}

dependencies {
    // Align all opentelemetry-kotlin artifacts
    api(platform(project(":bom")))

    // Import OpenTelemetry Java BOM version constraints. compat depends on both stable and alpha
    // (e.g. opentelemetry-sdk-extension-incubator) opentelemetry-java artifacts.
    api(platform(libs.opentelemetry.bom))
    api(platform(libs.opentelemetry.bom.alpha))
}