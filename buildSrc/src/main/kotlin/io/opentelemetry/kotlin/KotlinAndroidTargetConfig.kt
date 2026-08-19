package io.opentelemetry.kotlin

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project

fun Project.configureKotlinAndroidTarget(kotlinAndroidTarget: KotlinMultiplatformAndroidLibraryTarget) {
    kotlinAndroidTarget.apply {
        namespace = "io.opentelemetry.kotlin.${project.name.replace("-", ".")}"
        compileSdk = findVersion("android-compileSdk").toInt()
        minSdk = findVersion("android-minSdk").toInt()
        aarMetadata.minCompileSdk = findVersion("android-minCompileSdk").toInt()

        // currently bounded to min AGP for min supported Kotlin version
        // https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
        aarMetadata.minAgpVersion = findVersion("android-minAgpVersion")

        // Consumer keep rules are opt-in per module: any module that needs to ship R8 rules with
        // its AAR just adds a consumer-rules.pro alongside its build.gradle.kts.
        val consumerRules = project.file("consumer-rules.pro")
        if (consumerRules.exists()) {
            optimization {
                consumerKeepRules.file(consumerRules)

                // required, or the rules are not packaged into the AAR
                consumerKeepRules.publish = true
            }
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.configureCompiler()
            }
        }
    }
}