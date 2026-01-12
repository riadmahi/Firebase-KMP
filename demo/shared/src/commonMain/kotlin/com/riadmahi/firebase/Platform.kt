package com.riadmahi.firebase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform