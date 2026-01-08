package io.opentelemetry.kotlin

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project

fun Project.configurePublishing() {
    project.pluginManager.withPlugin("com.vanniktech.maven.publish") {
        val mavenPublishing = project.extensions.getByType(MavenPublishBaseExtension::class.java)

        mavenPublishing.apply {
            publishToMavenCentral()
            signAllPublications()
            coordinates("io.opentelemetry.kotlin", project.name, project.version.toString())

            pom {
                name.set("Opentelemetry Kotlin")
                description.set("An implementation of the OpenTelemetry specification as a Kotlin Multiplatform Library.")
                inceptionYear.set("2025")
                url.set("https://github.com/open-telemetry/opentelemetry-kotlin")

                licenses {
                    license {
                        name.set("Apache 2.0 License")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("opentelemetry")
                        name.set("OpenTelemetry")
                        url.set("https://github.com/open-telemetry/community")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/open-telemetry/opentelemetry-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/open-telemetry/opentelemetry-kotlin.git")
                    url.set("https://github.com/open-telemetry/opentelemetry-kotlin/tree/main")
                }
            }
        }
    }
}
