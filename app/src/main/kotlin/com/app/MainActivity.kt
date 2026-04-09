package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.View
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private external fun checkNativeHealth(): String
    private external fun startAdvancedScan(query: String, extraLink: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        // Pole: Czego szukamy
        val inputQuery = EditText(this).apply {
            hint = "Czego szukamy? (np. film.mkv)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }

        // Pole: Własna baza/link
        val inputLink = EditText(this).apply {
            hint = "Własny link do bazy (opcjonalnie)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }

        // Pasek postępu i czas
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
            progressDrawable.setTint(Color.parseColor("#00E5FF"))
        }

        val timerTv = TextView(this).apply {
            setTextColor(Color.LTGRAY)
            text = "Czas: 00:00 | Rozmiar: 0MB"
            visibility = View.GONE
        }

        val btnScan = Button(this).apply {
            text = "URUCHOM SILNIK SKANUJĄCY"
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.BLACK)
        }

        val resultsTv = TextView(this).apply {
            setTextColor(Color.WHITE)
            setPadding(0, 40, 0, 0)
        }

        btnScan.setOnClickListener {
            val query = inputQuery.text.toString()
            val link = inputLink.text.toString()
            
            if (query.isEmpty()) {
                Toast.makeText(this, "Wpisz czego szukasz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Symulacja startu
            progressBar.visibility = View.VISIBLE
            timerTv.visibility = View.VISIBLE
            progressBar.progress = 0
            
            val result = startAdvancedScan(query, link)
            resultsTv.text = result

            // Prosta symulacja postępu i rozmiaru (Punkt 6)
            var p = 0
            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    if (p <= 100) {
                        progressBar.progress = p
                        timerTv.text = "Czas: 00:${p/10} | Rozmiar: ${p*2}MB"
                        p += 5
                        handler.postDelayed(this, 500)
                    } else {
                        Toast.makeText(this@MainActivity, "Zapisano na dysk!", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

        root.addView(inputQuery)
        root.addView(inputLink)
        root.addView(btnScan)
        root.addView(progressBar)
        root.addView(timerTv)
        root.addView(ScrollView(this).apply { addView(resultsTv) })
        
        setContentView(root)
    }

    companion object {
        init {
            System.loadLibrary("videoscout")
        }
    }
}
