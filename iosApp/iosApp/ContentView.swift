import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let telemetry: FirebaseTelemetry

    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController(services: PlatformServices(telemetry: telemetry))
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    let telemetry: FirebaseTelemetry

    var body: some View {
        ComposeView(telemetry: telemetry)
            .ignoresSafeArea()
    }
}
