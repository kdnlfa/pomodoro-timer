package com.kdnlfa.pomodorotimer

object PomodoroSessionPlanner {

    private const val FOCUS_DURATION_MILLIS = 25L * 60L * 1000L
    private const val BREAK_DURATION_MILLIS = 5L * 60L * 1000L

    fun durationFor(phase: PomodoroPhase): Long {
        return when (phase) {
            PomodoroPhase.FOCUS -> FOCUS_DURATION_MILLIS
            PomodoroPhase.BREAK -> BREAK_DURATION_MILLIS
        }
    }

    fun nextPhase(after: PomodoroPhase): PomodoroPhase {
        return when (after) {
            PomodoroPhase.FOCUS -> PomodoroPhase.BREAK
            PomodoroPhase.BREAK -> PomodoroPhase.FOCUS
        }
    }
}

