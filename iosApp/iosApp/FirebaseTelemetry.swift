import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Foundation
import Shared

/// Firebase Analytics + Crashlytics behind the Kotlin `IosTelemetry` bridge (ADR 0023, docs/ANALYTICS.md).
final class FirebaseTelemetry: NSObject, IosTelemetry {
    private let telemetryAllowed: Bool

    private init(telemetryAllowed: Bool) {
        self.telemetryAllowed = telemetryAllowed
        super.init()
    }

    /// Configures Firebase once at launch. Info.plist keeps collection off; Release builds turn Crashlytics on, Debug
    /// builds only with the `-LexiTelemetry` launch argument. Analytics is switched off on every launch (the setter
    /// persists) and on only after the player's opt-in (`setAnalyticsCollectionEnabled`, called from Kotlin).
    static func start() -> FirebaseTelemetry {
        FirebaseApp.configure()
        #if DEBUG
        let allowed = ProcessInfo.processInfo.arguments.contains("-LexiTelemetry")
        #else
        let allowed = true
        #endif
        Analytics.setAnalyticsCollectionEnabled(false)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(allowed)
        return FirebaseTelemetry(telemetryAllowed: allowed)
    }

    func setAnalyticsCollectionEnabled(enabled: Bool) {
        Analytics.setAnalyticsCollectionEnabled(enabled && telemetryAllowed)
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
