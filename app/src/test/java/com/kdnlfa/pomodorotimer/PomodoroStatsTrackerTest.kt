package com.kdnlfa.pomodorotimer

import org.junit.Assert.assertEquals
import org.junit.Test

class PomodoroStatsTrackerTest {

    @Test
    fun updatedCompletedFocusSessions_incrementsAfterFocus() {
        assertEquals(
            3,
            PomodoroStatsTracker.updatedCompletedFocusSessions(2, PomodoroPhase.FOCUS)
        )
    }

    @Test
    fun updatedCompletedFocusSessions_doesNotIncrementAfterBreak() {
        assertEquals(
            2,
            PomodoroStatsTracker.updatedCompletedFocusSessions(2, PomodoroPhase.BREAK)
        )
    }

    @Test
    fun sanitizeCompletedFocusSessions_clampsNegativeValues() {
        assertEquals(0, PomodoroStatsTracker.sanitizeCompletedFocusSessions(-5))
    }
}
