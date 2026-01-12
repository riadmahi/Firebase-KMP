package com.riadmahi.firebase.demo

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object TimeUtils {
    actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
    actual fun currentTimeString(): String = NSDate().toString()
}
