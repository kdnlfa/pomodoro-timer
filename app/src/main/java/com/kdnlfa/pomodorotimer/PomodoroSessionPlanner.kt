package com.kdnlfa.pomodorotimer

object PomodoroSessionPlanner {

    const val DEFAULT_FOCUS_MINUTES = 25
    const val DEFAULT_BREAK_MINUTES = 5
    const val MIN_FOCUS_MINUTES = 10
    const val MAX_FOCUS_MINUTES = 60
    const val MIN_BREAK_MINUTES = 1
    const val MAX_BREAK_MINUTES = 30

    fun durationFor(
        phase: PomodoroPhase,
        focusMinutes: Int = DEFAULT_FOCUS_MINUTES,
        breakMinutes: Int = DEFAULT_BREAK_MINUTES
    ): Long {
        return when (phase) {
            PomodoroPhase.FOCUS -> clampFocusMinutes(focusMinutes).toMillis()
            PomodoroPhase.BREAK -> clampBreakMinutes(breakMinutes).toMillis()
        }
    }

    fun nextPhase(after: PomodoroPhase): PomodoroPhase {
        return when (after) {
            PomodoroPhase.FOCUS -> PomodoroPhase.BREAK
            PomodoroPhase.BREAK -> PomodoroPhase.FOCUS
        }
    }

    fun clampFocusMinutes(minutes: Int): Int {
        return minutes.coerceIn(MIN_FOCUS_MINUTES, MAX_FOCUS_MINUTES)
    }

    fun clampBreakMinutes(minutes: Int): Int {
        return minutes.coerceIn(MIN_BREAK_MINUTES, MAX_BREAK_MINUTES)
    }

    private fun Int.toMillis(): Long {
        return this.toLong() * 60L * 1000L
    }
}
