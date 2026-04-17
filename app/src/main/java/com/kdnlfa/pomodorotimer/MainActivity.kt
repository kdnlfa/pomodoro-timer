package com.kdnlfa.pomodorotimer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var phaseTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var autoStartSwitch: SwitchMaterial
    private lateinit var startPauseButton: MaterialButton
    private lateinit var resetButton: MaterialButton

    private var countdownTimer: CountDownTimer? = null
    private var currentPhase: PomodoroPhase = PomodoroPhase.FOCUS
    private var pendingTransitionFrom: PomodoroPhase? = null
    private var remainingMillis: Long = PomodoroSessionPlanner.durationFor(PomodoroPhase.FOCUS)
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
        autoStartSwitch = findViewById(R.id.autoStartSwitch)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        currentPhase = savedInstanceState
            ?.getString(KEY_CURRENT_PHASE)
            ?.let(PomodoroPhase::valueOf)
            ?: PomodoroPhase.FOCUS
        pendingTransitionFrom = savedInstanceState
            ?.getString(KEY_PENDING_TRANSITION_FROM)
            ?.let(PomodoroPhase::valueOf)
        remainingMillis = savedInstanceState?.getLong(KEY_REMAINING_MILLIS)
            ?: PomodoroSessionPlanner.durationFor(currentPhase)
        autoStartNextPhase = savedInstanceState?.getBoolean(KEY_AUTO_START_NEXT_PHASE)
            ?: preferences.getBoolean(PREFERENCE_AUTO_START_NEXT_PHASE, false)
        isRunning = savedInstanceState?.getBoolean(KEY_IS_RUNNING) ?: false

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
            remainingMillis = PomodoroSessionPlanner.durationFor(currentPhase)
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
        remainingMillis = PomodoroSessionPlanner.durationFor(currentPhase)
        countdownTimer = null
        isRunning = false

        val toastMessageRes = when {
            finishedPhase == PomodoroPhase.FOCUS && autoStartNextPhase ->
                R.string.focus_finished_auto_start
            finishedPhase == PomodoroPhase.FOCUS ->
                R.string.focus_finished_manual
            autoStartNextPhase ->
                R.string.break_finished_auto_start
            else ->
                R.string.break_finished_manual
        }

        if (autoStartNextPhase) {
            pendingTransitionFrom = null
            Toast.makeText(this, toastMessageRes, Toast.LENGTH_SHORT).show()
            startTimer()
        } else {
            pendingTransitionFrom = finishedPhase
            updateUi()
            Toast.makeText(this, toastMessageRes, Toast.LENGTH_SHORT).show()
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
        remainingMillis = PomodoroSessionPlanner.durationFor(currentPhase)
        isRunning = false
        updateUi()
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
        statusTextView.text = when {
            isRunning && currentPhase == PomodoroPhase.FOCUS -> getString(R.string.status_focus_running)
            isRunning && currentPhase == PomodoroPhase.BREAK -> getString(R.string.status_break_running)
            pendingTransitionFrom == PomodoroPhase.FOCUS -> getString(R.string.status_break_ready_after_focus)
            pendingTransitionFrom == PomodoroPhase.BREAK -> getString(R.string.status_focus_ready_after_break)
            remainingMillis == PomodoroSessionPlanner.durationFor(PomodoroPhase.FOCUS)
                && currentPhase == PomodoroPhase.FOCUS -> getString(R.string.status_focus_ready)
            remainingMillis == PomodoroSessionPlanner.durationFor(PomodoroPhase.BREAK)
                && currentPhase == PomodoroPhase.BREAK -> getString(R.string.status_break_ready)
            currentPhase == PomodoroPhase.FOCUS -> getString(R.string.status_focus_paused)
            else -> getString(R.string.status_break_paused)
        }
        startPauseButton.setText(
            when {
                isRunning -> R.string.action_pause
                currentPhase == PomodoroPhase.FOCUS &&
                    remainingMillis < PomodoroSessionPlanner.durationFor(PomodoroPhase.FOCUS) ->
                    R.string.action_continue_focus
                currentPhase == PomodoroPhase.BREAK &&
                    remainingMillis < PomodoroSessionPlanner.durationFor(PomodoroPhase.BREAK) ->
                    R.string.action_continue_break
                currentPhase == PomodoroPhase.FOCUS -> R.string.action_start_focus
                else -> R.string.action_start_break
            }
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "pomodoro_preferences"
        private const val PREFERENCE_AUTO_START_NEXT_PHASE = "autoStartNextPhase"
        private const val KEY_CURRENT_PHASE = "currentPhase"
        private const val KEY_PENDING_TRANSITION_FROM = "pendingTransitionFrom"
        private const val KEY_REMAINING_MILLIS = "remainingMillis"
        private const val KEY_AUTO_START_NEXT_PHASE = "autoStartNextPhase"
        private const val KEY_IS_RUNNING = "isRunning"
    }
}
