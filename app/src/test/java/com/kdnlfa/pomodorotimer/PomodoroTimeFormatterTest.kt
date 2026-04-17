package com.kdnlfa.pomodorotimer

import org.junit.Assert.assertEquals
import org.junit.Test

class PomodoroTimeFormatterTest {

    @Test
    fun format_fullPomodoro() {
        assertEquals("25:00", PomodoroTimeFormatter.format(25L * 60L * 1000L))
    }

    @Test
    fun format_roundsUpPartialSecond() {
        assertEquals("01:00", PomodoroTimeFormatter.format(59_001L))
    }

    @Test
    fun format_clampsNegativeValues() {
        assertEquals("00:00", PomodoroTimeFormatter.format(-1L))
    }
}
