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
    // Import OpenTelemetry Java BOM version constraints
    api(platform(libs.opentelemetry.bom))
}