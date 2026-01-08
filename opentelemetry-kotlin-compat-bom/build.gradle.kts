plugins {
    id("java-platform")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("io.opentelemetry.kotlin.build-logic")
}

description = "OpenTelemetry Kotlin Java Compatibility BOM"

dependencies {
    constraints {
        // Pin OpenTelemetry Java BOM version
        api(libs.opentelemetry.bom)
    }
}