package com.kdnlfa.pomodorotimer

object PomodoroStatsTracker {

    fun sanitizeCompletedFocusSessions(value: Int): Int {
        return value.coerceAtLeast(0)
    }

    fun updatedCompletedFocusSessions(
        completedFocusSessions: Int,
        finishedPhase: PomodoroPhase
    ): Int {
        val safeCount = sanitizeCompletedFocusSessions(completedFocusSessions)
        return if (finishedPhase == PomodoroPhase.FOCUS) {
            safeCount + 1
        } else {
            safeCount
        }
    }
}

