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

val publishSnapshotToMavenLocal = tasks.register<Exec>("publishSnapshotToMavenLocal") {
    workingDir = rootProject.projectDir
    val wrapper = when {
        System.getProperty("os.name").lowercase().contains("windows") -> "gradlew.bat"
        else -> "gradlew"
    }
    commandLine(
        rootProject.projectDir.resolve(wrapper).absolutePath,
        "publishToMavenLocal",
        "-Pversion=$snapshotVersion",
        "--no-daemon",
        "--quiet",
    )
}

tasks.test {
    useJUnitPlatform()
    dependsOn(publishSnapshotToMavenLocal)
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
