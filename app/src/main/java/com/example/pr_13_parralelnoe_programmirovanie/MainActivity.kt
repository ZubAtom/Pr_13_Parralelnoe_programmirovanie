package com.example.pr_13_parralelnoe_programmirovanie

// Реализуйте программу вычислений fn! со всеми десятичными знаками,
// где n in [1 ... 13], где fn – числа Фибоначчи, f1 = f2 = 1.

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private lateinit var answerTextView:TextView
    private var ans:String? = null

    private val one = 1.toBigInteger()
    private val zero = 0.toBigInteger()

    private var startScope: CoroutineScope? = null
    private lateinit var mainScope: CoroutineScope

    private val context = newSingleThreadContext(THREAD)

    private val _resultFlow = MutableStateFlow<BigInteger?>(null)
    private val resultFlow = _resultFlow.asStateFlow()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ANSWER_KEY, answerTextView.text.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        answerTextView = findViewById(R.id.answerTextView)
        mainScope = CoroutineScope(Dispatchers.Main)
        val answer = savedInstanceState?.getString(ANSWER_KEY)
        if (answer != null) {
            answerTextView.text = answer
            startScope = CoroutineScope(context)
            startScope?.launch {
                _resultFlow.emit(answer.toBigInteger())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        val numberTextView = findViewById<TextView>(R.id.editTextNumber)

        mainScope.launch {
            launch(context) {
                resultFlow.collect {
                    ans = it?.toString()
                }
            }
        }

        startButton.setOnClickListener {
            startScope?.cancel()
            startScope = CoroutineScope(context)
            startScope?.launch {
                launch {
                    calculation(numberTextView.text.toString().toIntOrNull())
                }
            }
        }

        stopButton.setOnClickListener {
            startScope?.cancel()
            mainScope.launch {
                answerTextView.text = ans
            }
        }
    }

    override fun onDestroy() {
        startScope?.cancel()
        mainScope.cancel()
        super.onDestroy()
    }

    private suspend fun calculation(n: Int?) {
        if (n != null) {
            if (n in 1..13) {
                val numbers = generateSequence(1) { if (it < n) it + 1 else null }
                val numbersFact = numbers.map {
                    numbers.take(it).fold(one) { acc: BigInteger, num: Int ->
                        acc * num.toBigInteger()
                    }
                }
                var f = zero
                var s = one
                var i = zero
                startScope?.launch {
                    while (isActive && i++ < numbersFact.elementAt(n - 1)) {
                        s += f
                        f = s - f
                        _resultFlow.emit(f)
                    }
                }
            } else
                startScope?.launch {
                    _resultFlow.emit(null)
                }
        } else
            startScope?.launch {
                _resultFlow.emit(null)
            }
    }

    companion object{
        const val ANSWER_KEY = "ANSWER"
        const val THREAD = "thread"
    }
}





