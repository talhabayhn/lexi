package dev.talhabayhan.lexi

import dev.talhabayhan.lexi.telemetry.IosTelemetry

/**
 * Services implemented in Swift and handed to [MainViewController] (`ARCHITECTURE.md` §7). Part of the Swift API:
 * created in `iOSApp.swift`.
 */
class PlatformServices(val telemetry: IosTelemetry)

/** Set once by [MainViewController] before the first composition; read by the iOS platform module. */
internal object IosPlatformServices {
    private var current: PlatformServices? = null

    val services: PlatformServices get() = checkNotNull(current) { "MainViewController(services) was not called" }

    fun install(services: PlatformServices) {
        current = services
    }
}
