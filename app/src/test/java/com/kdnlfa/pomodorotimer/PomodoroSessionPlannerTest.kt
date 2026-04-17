package com.kdnlfa.pomodorotimer

import org.junit.Assert.assertEquals
import org.junit.Test

class PomodoroSessionPlannerTest {

    @Test
    fun durationFor_focusPhase_isTwentyFiveMinutes() {
        assertEquals(25L * 60L * 1000L, PomodoroSessionPlanner.durationFor(PomodoroPhase.FOCUS))
    }

    @Test
    fun durationFor_breakPhase_isFiveMinutes() {
        assertEquals(5L * 60L * 1000L, PomodoroSessionPlanner.durationFor(PomodoroPhase.BREAK))
    }

    @Test
    fun nextPhase_afterFocus_returnsBreak() {
        assertEquals(PomodoroPhase.BREAK, PomodoroSessionPlanner.nextPhase(PomodoroPhase.FOCUS))
    }

    @Test
    fun nextPhase_afterBreak_returnsFocus() {
        assertEquals(PomodoroPhase.FOCUS, PomodoroSessionPlanner.nextPhase(PomodoroPhase.BREAK))
    }
}
