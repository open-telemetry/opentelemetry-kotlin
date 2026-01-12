plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        nodejs {
            binaries.executable()
        }
        browser {
            binaries.executable()
            commonWebpackConfig {
                outputFileName = "js-app.js"
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":examples:example-common"))
                implementation(project(":core"))
            }
        }
    }
}
