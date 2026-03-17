import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    id("com.android.kotlin.multiplatform.library") apply false
    id("org.jetbrains.kotlin.multiplatform") apply false
    id("com.vanniktech.maven.publish") apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
}

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        resolution("minimatch", ">=9.0.7")
        resolution("serialize-javascript", ">=7.0.3")
        resolution("glob", ">=10.5.0")
        resolution("js-yaml", ">=4.1.1")
        resolution("diff", ">=8.0.3")
    }
}

group = "io.opentelemetry.kotlin"
version = project.version

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
                classes("*.BuildConfig", "io.opentelemetry.proto.*")
            }
        }
    }
}
