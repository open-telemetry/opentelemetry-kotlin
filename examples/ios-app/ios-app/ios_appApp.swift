import SwiftUI
import OtelKotlinExample
import OtelKotlinApi
import OtelKotlinApiExt
import OtelKotlinImplementation

@main
struct ios_appApp: App {

    init() {
        let api = OtelKotlinExampleKt.instantiateOtelApi()
        OtelKotlinExampleKt.runLoggingExamples(api: api)
        OtelKotlinExampleKt.runTracingExamples(api: api)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
