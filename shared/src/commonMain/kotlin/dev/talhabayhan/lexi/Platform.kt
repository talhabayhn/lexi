package dev.talhabayhan.lexi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
