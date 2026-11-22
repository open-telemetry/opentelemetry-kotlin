// region getting-started-ios-swift
import SwiftUI
import Shared

@main
struct MyApp: App {
    init() {
        Telemetry.shared.start()
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
// end-region
