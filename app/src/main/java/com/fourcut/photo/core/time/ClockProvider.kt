package com.fourcut.photo.core.time

fun interface ClockProvider {
    fun nowMillis(): Long
}
