package com.cyclesync.core.utils

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
    if (name.isNullOrBlank()) return null
    return try {
        enumValueOf<T>(name.uppercase())
    } catch (_: IllegalArgumentException) {
        null
    }
}
