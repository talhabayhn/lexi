import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Foundation
import Shared

/// Firebase Analytics + Crashlytics behind the Kotlin `IosTelemetry` bridge (ADR 0023, docs/ANALYTICS.md).
final class FirebaseTelemetry: NSObject, IosTelemetry {
    /// Configures Firebase once at launch. Info.plist keeps collection off; Release builds turn it on, Debug builds
    /// only with the `-LexiTelemetry` launch argument. Both setters persist, so they are set on every launch.
    static func start() -> FirebaseTelemetry {
        FirebaseApp.configure()
        #if DEBUG
        let enabled = ProcessInfo.processInfo.arguments.contains("-LexiTelemetry")
        #else
        let enabled = true
        #endif
        Analytics.setAnalyticsCollectionEnabled(enabled)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
        return FirebaseTelemetry()
    }

    func logEvent(name: String, params: [String: Any]) {
        Analytics.logEvent(name, parameters: params)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func recordException(type: String, message: String, stackTrace: [String]) {
        let model = ExceptionModel(name: type, reason: message)
        model.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }
}
