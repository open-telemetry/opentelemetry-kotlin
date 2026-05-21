plugins {
    kotlin("jvm")
}

ext["io.opentelemetry.kotlin.enableCodeCoverage"] = false

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val minSupportedGradle = libs.versions.minSupportedGradle.get()
val snapshotVersion = "${project.version}-SNAPSHOT"
val catalogFile = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml").asFile

// snapshot artifacts consumed by the test must already be installed in mavenLocal
// In CI this is wired as two steps as running via Gradle's Exec from inside this build caused
// Kotlin/Native klib writes to race against the outer build's `build/` directories.

tasks.test {
    useJUnitPlatform()
    systemProperty("otelKotlinVersion", snapshotVersion)
    systemProperty("minSupportedGradle", minSupportedGradle)
    systemProperty("catalogPath", catalogFile.absolutePath)
    systemProperty("fixtureSrcDir", layout.projectDirectory.dir("src/test/resources/fixtures").asFile.absolutePath)

    // if version catalog changes, invalidate the build so that the test case reruns
    inputs.dir(layout.projectDirectory.dir("src/test/resources/fixtures"))
        .withPropertyName("fixtures")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(catalogFile)
        .withPropertyName("catalog")
        .withPathSensitivity(PathSensitivity.NONE)
    testLogging {
        showStandardStreams = true
        showStackTraces = false
    }
}
