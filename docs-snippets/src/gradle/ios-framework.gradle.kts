// region getting-started-ios-framework
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared" // the module name Swift imports
        }
    }
}
// end-region
