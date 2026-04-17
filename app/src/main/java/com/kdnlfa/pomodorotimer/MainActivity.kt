package com.kdnlfa.pomodorotimer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var phaseTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var focusDurationValueTextView: TextView
    private lateinit var breakDurationValueTextView: TextView
    private lateinit var settingsHintTextView: TextView
    private lateinit var focusDurationSlider: Slider
    private lateinit var breakDurationSlider: Slider
    private lateinit var autoStartSwitch: SwitchMaterial
    private lateinit var startPauseButton: MaterialButton
    private lateinit var resetButton: MaterialButton

    private var countdownTimer: CountDownTimer? = null
    private var currentPhase: PomodoroPhase = PomodoroPhase.FOCUS
    private var pendingTransitionFrom: PomodoroPhase? = null
    private var focusMinutes: Int = PomodoroSessionPlanner.DEFAULT_FOCUS_MINUTES
    private var breakMinutes: Int = PomodoroSessionPlanner.DEFAULT_BREAK_MINUTES
    private var remainingMillis: Long = durationFor(PomodoroPhase.FOCUS)
    private var autoStartNextPhase: Boolean = false
    private var isRunning: Boolean = false
    private val preferences by lazy {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phaseTextView = findViewById(R.id.phaseTextView)
        timerTextView = findViewById(R.id.timerTextView)
        statusTextView = findViewById(R.id.statusTextView)
        focusDurationValueTextView = findViewById(R.id.focusDurationValueTextView)
        breakDurationValueTextView = findViewById(R.id.breakDurationValueTextView)
        settingsHintTextView = findViewById(R.id.settingsHintTextView)
        focusDurationSlider = findViewById(R.id.focusDurationSlider)
        breakDurationSlider = findViewById(R.id.breakDurationSlider)
        autoStartSwitch = findViewById(R.id.autoStartSwitch)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        focusMinutes = savedInstanceState?.getInt(KEY_FOCUS_MINUTES)
            ?: preferences.getInt(
                PREFERENCE_FOCUS_MINUTES,
                PomodoroSessionPlanner.DEFAULT_FOCUS_MINUTES
            )
        breakMinutes = savedInstanceState?.getInt(KEY_BREAK_MINUTES)
            ?: preferences.getInt(
                PREFERENCE_BREAK_MINUTES,
                PomodoroSessionPlanner.DEFAULT_BREAK_MINUTES
            )
        focusMinutes = PomodoroSessionPlanner.clampFocusMinutes(focusMinutes)
        breakMinutes = PomodoroSessionPlanner.clampBreakMinutes(breakMinutes)
        currentPhase = savedInstanceState
            ?.getString(KEY_CURRENT_PHASE)
            ?.let(PomodoroPhase::valueOf)
            ?: PomodoroPhase.FOCUS
        pendingTransitionFrom = savedInstanceState
            ?.getString(KEY_PENDING_TRANSITION_FROM)
            ?.let(PomodoroPhase::valueOf)
        remainingMillis = savedInstanceState?.getLong(KEY_REMAINING_MILLIS)
            ?: durationFor(currentPhase)
        autoStartNextPhase = savedInstanceState?.getBoolean(KEY_AUTO_START_NEXT_PHASE)
            ?: preferences.getBoolean(PREFERENCE_AUTO_START_NEXT_PHASE, false)
        isRunning = savedInstanceState?.getBoolean(KEY_IS_RUNNING) ?: false

        focusDurationSlider.valueFrom = PomodoroSessionPlanner.MIN_FOCUS_MINUTES.toFloat()
        focusDurationSlider.valueTo = PomodoroSessionPlanner.MAX_FOCUS_MINUTES.toFloat()
        focusDurationSlider.stepSize = 5f
        breakDurationSlider.valueFrom = PomodoroSessionPlanner.MIN_BREAK_MINUTES.toFloat()
        breakDurationSlider.valueTo = PomodoroSessionPlanner.MAX_BREAK_MINUTES.toFloat()
        breakDurationSlider.stepSize = 1f
        focusDurationSlider.value = focusMinutes.toFloat()
        breakDurationSlider.value = breakMinutes.toFloat()
        focusDurationSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateDurationPreference(PomodoroPhase.FOCUS, value.toInt())
            }
        }
        breakDurationSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateDurationPreference(PomodoroPhase.BREAK, value.toInt())
            }
        }

        autoStartSwitch.isChecked = autoStartNextPhase
        autoStartSwitch.setOnCheckedChangeListener { _, isChecked ->
            autoStartNextPhase = isChecked
            preferences.edit()
                .putBoolean(PREFERENCE_AUTO_START_NEXT_PHASE, isChecked)
                .apply()
            updateUi()
        }

        startPauseButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        updateUi()

        if (isRunning) {
            startTimer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_FOCUS_MINUTES, focusMinutes)
        outState.putInt(KEY_BREAK_MINUTES, breakMinutes)
        outState.putString(KEY_CURRENT_PHASE, currentPhase.name)
        outState.putString(KEY_PENDING_TRANSITION_FROM, pendingTransitionFrom?.name)
        outState.putLong(KEY_REMAINING_MILLIS, remainingMillis)
        outState.putBoolean(KEY_AUTO_START_NEXT_PHASE, autoStartNextPhase)
        outState.putBoolean(KEY_IS_RUNNING, isRunning)
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        countdownTimer = null
        super.onDestroy()
    }

    private fun startTimer() {
        if (remainingMillis <= 0L) {
            remainingMillis = durationFor(currentPhase)
        }

        countdownTimer?.cancel()
        isRunning = true
        pendingTransitionFrom = null
        updateUi()

        countdownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateUi()
            }

            override fun onFinish() {
                handlePhaseFinished(currentPhase)
            }
        }.start()
    }

    private fun handlePhaseFinished(finishedPhase: PomodoroPhase) {
        currentPhase = PomodoroSessionPlanner.nextPhase(finishedPhase)
        remainingMillis = durationFor(currentPhase)
        countdownTimer = null
        isRunning = false
        val finishedMinutes = when (finishedPhase) {
            PomodoroPhase.FOCUS -> focusMinutes
            PomodoroPhase.BREAK -> breakMinutes
        }

        val toastMessage = when {
            finishedPhase == PomodoroPhase.FOCUS && autoStartNextPhase ->
                getString(R.string.focus_finished_auto_start, finishedMinutes)
            finishedPhase == PomodoroPhase.FOCUS ->
                getString(R.string.focus_finished_manual, finishedMinutes)
            autoStartNextPhase ->
                getString(R.string.break_finished_auto_start, finishedMinutes)
            else ->
                getString(R.string.break_finished_manual, finishedMinutes)
        }

        if (autoStartNextPhase) {
            pendingTransitionFrom = null
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            startTimer()
        } else {
            pendingTransitionFrom = finishedPhase
            updateUi()
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        isRunning = false
        updateUi()
    }

    private fun resetTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        currentPhase = PomodoroPhase.FOCUS
        pendingTransitionFrom = null
        remainingMillis = durationFor(currentPhase)
        isRunning = false
        updateUi()
    }

    private fun updateDurationPreference(phase: PomodoroPhase, requestedMinutes: Int) {
        val previousDuration = durationFor(phase)
        val clampedMinutes = when (phase) {
            PomodoroPhase.FOCUS -> PomodoroSessionPlanner.clampFocusMinutes(requestedMinutes)
            PomodoroPhase.BREAK -> PomodoroSessionPlanner.clampBreakMinutes(requestedMinutes)
        }

        when (phase) {
            PomodoroPhase.FOCUS -> {
                focusMinutes = clampedMinutes
                preferences.edit().putInt(PREFERENCE_FOCUS_MINUTES, clampedMinutes).apply()
            }

            PomodoroPhase.BREAK -> {
                breakMinutes = clampedMinutes
                preferences.edit().putInt(PREFERENCE_BREAK_MINUTES, clampedMinutes).apply()
            }
        }

        if (!isRunning && currentPhase == phase) {
            val updatedDuration = durationFor(phase)
            remainingMillis = if (pendingTransitionFrom != null || remainingMillis == previousDuration) {
                updatedDuration
            } else {
                remainingMillis.coerceAtMost(updatedDuration)
            }
        }

        updateUi()
    }

    private fun durationFor(phase: PomodoroPhase): Long {
        return PomodoroSessionPlanner.durationFor(
            phase = phase,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes
        )
    }

    private fun updateUi() {
        phaseTextView.text = getString(
            if (currentPhase == PomodoroPhase.FOCUS) {
                R.string.phase_focus
            } else {
                R.string.phase_break
            }
        )
        timerTextView.text = PomodoroTimeFormatter.format(remainingMillis)
        focusDurationValueTextView.text = getString(R.string.minutes_value, focusMinutes)
        breakDurationValueTextView.text = getString(R.string.minutes_value, breakMinutes)
        settingsHintTextView.text = getString(
            if (isRunning) {
                R.string.settings_hint_locked
            } else {
                R.string.settings_hint_saved
            }
        )
        focusDurationSlider.isEnabled = !isRunning
        breakDurationSlider.isEnabled = !isRunning
        statusTextView.text = when {
            isRunning && currentPhase == PomodoroPhase.FOCUS -> getString(R.string.status_focus_running)
            isRunning && currentPhase == PomodoroPhase.BREAK -> getString(R.string.status_break_running)
            pendingTransitionFrom == PomodoroPhase.FOCUS ->
                getString(R.string.status_break_ready_after_focus, breakMinutes)
            pendingTransitionFrom == PomodoroPhase.BREAK ->
                getString(R.string.status_focus_ready_after_break, focusMinutes)
            remainingMillis == durationFor(PomodoroPhase.FOCUS)
                && currentPhase == PomodoroPhase.FOCUS ->
                getString(R.string.status_focus_ready, focusMinutes)
            remainingMillis == durationFor(PomodoroPhase.BREAK)
                && currentPhase == PomodoroPhase.BREAK ->
                getString(R.string.status_break_ready, breakMinutes)
            currentPhase == PomodoroPhase.FOCUS -> getString(R.string.status_focus_paused)
            else -> getString(R.string.status_break_paused)
        }
        startPauseButton.setText(
            when {
                isRunning -> R.string.action_pause
                currentPhase == PomodoroPhase.FOCUS &&
                    remainingMillis < durationFor(PomodoroPhase.FOCUS) ->
                    R.string.action_continue_focus
                currentPhase == PomodoroPhase.BREAK &&
                    remainingMillis < durationFor(PomodoroPhase.BREAK) ->
                    R.string.action_continue_break
                currentPhase == PomodoroPhase.FOCUS -> R.string.action_start_focus
                else -> R.string.action_start_break
            }
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "pomodoro_preferences"
        private const val PREFERENCE_AUTO_START_NEXT_PHASE = "autoStartNextPhase"
        private const val PREFERENCE_FOCUS_MINUTES = "focusMinutes"
        private const val PREFERENCE_BREAK_MINUTES = "breakMinutes"
        private const val KEY_FOCUS_MINUTES = "focusMinutes"
        private const val KEY_BREAK_MINUTES = "breakMinutes"
        private const val KEY_CURRENT_PHASE = "currentPhase"
        private const val KEY_PENDING_TRANSITION_FROM = "pendingTransitionFrom"
        private const val KEY_REMAINING_MILLIS = "remainingMillis"
        private const val KEY_AUTO_START_NEXT_PHASE = "autoStartNextPhase"
        private const val KEY_IS_RUNNING = "isRunning"
    }
}
