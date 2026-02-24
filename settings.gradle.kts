pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
