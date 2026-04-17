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

    @Test
    fun durationFor_usesCustomFocusMinutes() {
        assertEquals(
            40L * 60L * 1000L,
            PomodoroSessionPlanner.durationFor(
                phase = PomodoroPhase.FOCUS,
                focusMinutes = 40,
                breakMinutes = 5
            )
        )
    }

    @Test
    fun clampFocusMinutes_limitsValuesToSupportedRange() {
        assertEquals(10, PomodoroSessionPlanner.clampFocusMinutes(1))
        assertEquals(60, PomodoroSessionPlanner.clampFocusMinutes(90))
    }

    @Test
    fun clampBreakMinutes_limitsValuesToSupportedRange() {
        assertEquals(1, PomodoroSessionPlanner.clampBreakMinutes(0))
        assertEquals(30, PomodoroSessionPlanner.clampBreakMinutes(45))
    }
}
