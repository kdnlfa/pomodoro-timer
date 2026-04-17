package com.kdnlfa.pomodorotimer

import java.util.Locale

object PomodoroTimeFormatter {

    fun format(totalMillis: Long): String {
        val safeMillis = totalMillis.coerceAtLeast(0L)
        val totalSeconds = if (safeMillis == 0L) 0L else (safeMillis + 999L) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

