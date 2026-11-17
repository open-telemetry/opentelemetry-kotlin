plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(11)
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":exporters-protobuf"))
                implementation(libs.wire.runtime)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.opentelemetry.example.collector.MainKt"
    }
}

// Prints received telemetry to stdout without opening a window.
tasks.register<JavaExec>("runHeadless") {
    dependsOn("jvmMainClasses")
    val compilation = kotlin.targets.named("jvm").get().compilations.named("main").get()
    classpath(compilation.output.allOutputs, compilation.runtimeDependencyFiles)
    mainClass.set("io.opentelemetry.example.collector.MainKt")
    args("--headless")
}
