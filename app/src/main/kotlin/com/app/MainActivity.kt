package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity

class MainActivity : AppCompatActivity() {

    private external fun checkNativeHealth(): String
    private external fun startVideoScan(query: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Główne kontenery (Punkt 4 - struktura)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            backgroundColor = Color.parseColor("#121212")
        }

        val statusTv = TextView(this).apply {
            text = checkNativeHealth()
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 14f
        }

        val btnScan = Button(this).apply {
            text = "ROZPOCZNIJ SKANOWANIE"
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.BLACK)
        }

        val resultsTv = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(0, 32, 0, 0)
        }

        val scrollView = ScrollView(this).apply {
            addView(resultsTv)
        }

        // Obsługa kliknięcia (Punkt 6)
        btnScan.setOnClickListener {
            val res = startVideoScan("Video_Search_Alpha")
            resultsTv.text = res
            Toast.makeText(this, "Skanowanie ukończone", Toast.LENGTH_SHORT).show()
        }

        root.addView(statusTv)
        root.addView(btnScan)
        root.addView(scrollView)
        setContentView(root)
    }

    companion object {
        init {
            System.loadLibrary("videoscout")
        }
    }
}
