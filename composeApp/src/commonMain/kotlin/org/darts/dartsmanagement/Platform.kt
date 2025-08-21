package org.darts.dartsmanagement

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform