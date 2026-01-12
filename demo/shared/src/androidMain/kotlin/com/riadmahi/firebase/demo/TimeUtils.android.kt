package com.riadmahi.firebase.demo

import kotlinx.datetime.Clock

actual object TimeUtils {
    actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
    actual fun currentTimeString(): String = Clock.System.now().toString()
}
