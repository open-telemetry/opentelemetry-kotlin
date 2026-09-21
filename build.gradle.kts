import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    id("com.android.kotlin.multiplatform.library") apply false
    id("org.jetbrains.kotlin.multiplatform") apply false
    id("com.vanniktech.maven.publish") apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
}

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        resolution("brace-expansion", ">=5.0.8")
        resolution("diff", ">=8.0.3")
        resolution("glob", ">=10.5.0")
        resolution("js-yaml", ">=5.2.2")
        resolution("minimatch", ">=9.0.7")
        resolution("serialize-javascript", ">=7.0.5")
        resolution("**/ws", "8.21.0")
        resolution("ws", "8.21.0")
    }
}

group = "io.opentelemetry.kotlin"
version = project.version

val publishingToMavenLocal = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':') == "publishToMavenLocal"
}
val snapshotPublish = project.findProperty("snapshotPublish")
    ?.toString()
    ?.toBoolean()
    ?: publishingToMavenLocal

if (snapshotPublish) {
    allprojects {
        version = "${version}-SNAPSHOT"
    }
}

kover {
    merge {
        subprojects { project ->
            project.findProperty("io.opentelemetry.kotlin.enableCodeCoverage")?.toString()?.toBoolean() ?: true
        }
    }
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                // generated code: protobuf messages and the opentelemetry-configuration schema model
                classes(
                    "*.BuildConfig",
                    "io.opentelemetry.proto.*",
                    "io.opentelemetry.kotlin.config.schema.model.*",
                )
            }
        }
    }
}
