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

    // "Pamięć" ciasteczek (Punkt 6)
    private val sessionCookies = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val input = EditText(this).apply {
            hint = "Wpisz tytuł (np. Mavka)"
            setTextColor(Color.WHITE)
        }

        val btnSearch = Button(this).apply { text = "SKANUJ SIECI (STEALTH MODE)" }
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        btnSearch.setOnClickListener {
            val tytul = input.text.toString()
            listContainer.removeAllViews()
            
            thread {
                try {
                    // Wyrafinowany dork usuwający VOD z wyników wyszukiwania
                    val query = "$tytul lektor pl -inurl:player.pl -inurl:netflix -inurl:disneyplus"
                    val url = "https://html.duckduckgo.com/html/?q=" + Uri.encode(query)
                    
                    val response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36")
                        .header("Accept-Language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7")
                        .execute()
                    
                    sessionCookies.putAll(response.cookies())
                    val doc = response.parse()
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
                    runOnUiThread { Toast.makeText(this@MainActivity, "Błąd DuckDuckGo: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        root.addView(input); root.addView(btnSearch); root.addView(scroll)
        setContentView(root)
    }

    private fun analyzeStream(url: String) {
        Toast.makeText(this, "Analiza Stealth: $url", Toast.LENGTH_SHORT).show()
        thread {
            try {
                // Udawanie sesji z ciasteczkami i Refererem (Punkt 6)
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36")
                    .referrer("https://duckduckgo.com/")
                    .cookies(sessionCookies)
                    .timeout(8000)
                    .ignoreHttpErrors(true)
                    .get()

                val videoTags = doc.select("video source, iframe, a[href~=(?i)\\.(mp4|mkv)]")
                
                runOnUiThread {
                    if (videoTags.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Wykryto zabezpieczenia lub brak pliku. Zmień źródło.", Toast.LENGTH_LONG).show()
                    } else {
                        val firstFound = videoTags.first()?.attr("src") ?: videoTags.first()?.attr("href")
                        Toast.makeText(this@MainActivity, "SUKCES: $firstFound", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Blokada Captcha/Bot: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    companion object { init { System.loadLibrary("videoscout") } }
}
