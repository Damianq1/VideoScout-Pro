package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Deklaracja funkcji natywnej z discovery.cpp
    private external fun checkNativeHealth(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tv = TextView(this)
        
        try {
            // Próba wywołania funkcji z C++
            val healthStatus = checkNativeHealth()
            tv.text = healthStatus
            Toast.makeText(this, healthStatus, Toast.LENGTH_LONG).show()
        } catch (e: UnsatisfiedLinkError) {
            tv.text = "Błąd: Nie znaleziono biblioteki natywnej"
        }

        setContentView(tv)
    }

    companion object {
        // Ładowanie skompilowanego pliku .so
        init {
            System.loadLibrary("videoscout")
        }
    }
}
