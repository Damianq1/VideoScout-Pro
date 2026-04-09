package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private external fun checkNativeHealth(): String
    private external fun getFormatCount(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val statusText = TextView(this).apply {
            text = checkNativeHealth()
            textSize = 18f
        }

        val scanButton = Button(this).apply {
            text = "Rozpocznij Skanowanie"
            setOnClickListener {
                val count = getFormatCount()
                Toast.makeText(context, "Skaner obsługuje $count formatów wideo", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(statusText)
        layout.addView(scanButton)
        setContentView(layout)
    }

    companion object {
        init {
            System.loadLibrary("videoscout")
        }
    }
}
