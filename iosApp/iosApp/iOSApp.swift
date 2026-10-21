import SwiftUI

@main
struct iOSApp: App {
    /// Firebase is configured before the first Compose frame (ADR 0023).
    private let telemetry = FirebaseTelemetry.start()

    var body: some Scene {
        WindowGroup {
            ContentView(telemetry: telemetry)
        }
    }
}
