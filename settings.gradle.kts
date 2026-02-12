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
    ":sdk",
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
    "examples:jvm-app",
    "examples:js-app",
    "examples:android-app",
    "examples:example-common"
)
