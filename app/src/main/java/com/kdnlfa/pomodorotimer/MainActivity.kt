package com.kdnlfa.pomodorotimer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var startPauseButton: MaterialButton
    private lateinit var resetButton: MaterialButton

    private var countdownTimer: CountDownTimer? = null
    private var remainingMillis: Long = WORK_DURATION_MILLIS
    private var isRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.timerTextView)
        statusTextView = findViewById(R.id.statusTextView)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        remainingMillis = savedInstanceState?.getLong(KEY_REMAINING_MILLIS) ?: WORK_DURATION_MILLIS
        isRunning = savedInstanceState?.getBoolean(KEY_IS_RUNNING) ?: false

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
        outState.putLong(KEY_REMAINING_MILLIS, remainingMillis)
        outState.putBoolean(KEY_IS_RUNNING, isRunning)
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        countdownTimer = null
        super.onDestroy()
    }

    private fun startTimer() {
        if (remainingMillis <= 0L) {
            remainingMillis = WORK_DURATION_MILLIS
        }

        countdownTimer?.cancel()
        isRunning = true
        updateUi()

        countdownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateUi()
            }

            override fun onFinish() {
                remainingMillis = 0L
                isRunning = false
                countdownTimer = null
                updateUi()
                Toast.makeText(
                    this@MainActivity,
                    R.string.pomodoro_finished,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()
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
        remainingMillis = WORK_DURATION_MILLIS
        isRunning = false
        updateUi()
    }

    private fun updateUi() {
        timerTextView.text = PomodoroTimeFormatter.format(remainingMillis)
        statusTextView.text = when {
            remainingMillis == 0L -> getString(R.string.status_finished)
            isRunning -> getString(R.string.status_running)
            remainingMillis == WORK_DURATION_MILLIS -> getString(R.string.status_ready)
            else -> getString(R.string.status_paused)
        }
        startPauseButton.setText(
            when {
                isRunning -> R.string.action_pause
                remainingMillis == 0L -> R.string.action_restart
                else -> R.string.action_start
            }
        )
    }

    companion object {
        private const val WORK_DURATION_MILLIS = 25L * 60L * 1000L
        private const val KEY_REMAINING_MILLIS = "remainingMillis"
        private const val KEY_IS_RUNNING = "isRunning"
    }
}

