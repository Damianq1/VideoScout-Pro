package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.View

class MainActivity : AppCompatActivity() {

    private external fun fetchAvailableStreams(query: String): String
    private external fun startSelectedDownload(streamId: String, savePath: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val inputQuery = EditText(this).apply {
            hint = "Wpisz nazwę filmu..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        val btnSearch = Button(this).apply { text = "SZUKAJ DOSTĘPNYCH PLIKÓW" }
        
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        btnSearch.setOnClickListener {
            listContainer.removeAllViews()
            val rawData = fetchAvailableStreams(inputQuery.text.toString())
            
            // Punkt 6: Parsowanie wyników z C++ i tworzenie przycisków wyboru
            rawData.split("\n").forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    val btn = Button(this).apply {
                        text = "POBIERZ: ${parts[1]} (${parts[2]})"
                        setBackgroundColor(Color.parseColor("#333333"))
                        setTextColor(Color.WHITE)
                        setOnClickListener {
                            val path = getExternalFilesDir(null)?.absolutePath ?: "/sdcard"
                            val savedFile = startSelectedDownload(parts[0], path)
                            Toast.makeText(context, "Pobieranie: ${parts[1]}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    listContainer.addView(btn)
                }
            }
        }

        root.addView(inputQuery)
        root.addView(btnSearch)
        root.addView(scroll)
        setContentView(root)
    }

    companion object {
        init {
            System.loadLibrary("videoscout")
        }
    }
}
