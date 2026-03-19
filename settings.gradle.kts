pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "opentelemetry-kotlin"
include(
    ":core",
    ":api",
    ":api-ext",
    ":sdk-api",
    ":sdk-common",
    ":noop",
    ":implementation",
    ":model",
    ":compat",
    ":platform-implementations",
    ":testing",
    ":test-fakes",
    ":integration-test",
    ":compat-bom",
    ":semconv",
    ":benchmark-android",
    ":benchmark-jvm",
    ":benchmark-fixtures",
    ":exporters-core",
    ":exporters-in-memory",
    ":exporters-otlp",
    ":exporters-persistence",
    ":exporters-protobuf",
    ":java-typealiases",
    "examples:example-app",
    "examples:example-app-android",
    ":smoke-test",
)

includeFromDir("instrumentation")

fun includeFromDir(dirName: String, maxDepth: Int = 3) {
    val dir = File(rootDir, dirName)
    val separator = Regex("[/\\\\]")
    dir.walk().maxDepth(maxDepth).forEach {
        if (it.name == "build.gradle.kts") {
            include(":$dirName:${it.parentFile.toRelativeString(dir).replace(separator, ":")}")
        }
    }
}
