package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import org.jsoup.Jsoup
import kotlin.concurrent.thread
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private external fun isDomainSafe(domain: String): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val input = EditText(this).apply {
            hint = "Wpisz tytuł (np. Vaiana)"
            setTextColor(Color.WHITE)
        }

        val btnSearch = Button(this).apply { text = "SKANUJ SIECI" }
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        btnSearch.setOnClickListener {
            val tytul = input.text.toString()
            listContainer.removeAllViews()
            
            thread {
                try {
                    val query = "$tytul lektor pl -netflix -disney -player"
                    val url = "https://html.duckduckgo.com/html/?q=" + Uri.encode(query)
                    val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
                    val links = doc.select(".result__a")

                    runOnUiThread {
                        links.forEach { element ->
                            val linkUrl = element.attr("href")
                            val domain = Uri.parse(linkUrl).host?.replace("www.", "") ?: ""

                            if (isDomainSafe(domain)) {
                                val btn = Button(this@MainActivity).apply {
                                    text = "ŹRÓDŁO: $domain\n${element.text()}"
                                    setTextColor(Color.WHITE)
                                    setBackgroundColor(Color.parseColor("#2C2C2C"))
                                    setOnClickListener { analyzeStream(linkUrl) }
                                }
                                listContainer.addView(btn)
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this@MainActivity, "Błąd skanowania", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        root.addView(input); root.addView(btnSearch); root.addView(scroll)
        setContentView(root)
    }

    private fun analyzeStream(url: String) {
        Toast.makeText(this, "Szukanie wideo na: $url", Toast.LENGTH_SHORT).show()
        thread {
            try {
                val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get()
                // POPRAWKA: Podwójny backslash przed kropką (\\.)
                val videoTags = doc.select("video source, iframe, a[href~=(?i)\\.(mp4|mkv)]")
                
                runOnUiThread {
                    if (videoTags.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Nie znaleziono strumienia. Spróbuj inne źródło.", Toast.LENGTH_LONG).show()
                    } else {
                        val firstFound = videoTags.first()?.attr("src") ?: videoTags.first()?.attr("href")
                        Toast.makeText(this@MainActivity, "Wykryto: $firstFound", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Strona zablokowała skanowanie", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    companion object { init { System.loadLibrary("videoscout") } }
}
