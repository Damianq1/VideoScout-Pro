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
            hint = "Wpisz tytuł (np. Mavka)"
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
                    val url = "https://html.duckduckgo.com/html/?q=" + Uri.encode("$tytul lektor pl")
                    val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
                    val links = doc.select(".result__a")

                    runOnUiThread {
                        links.forEach { element ->
                            val linkUrl = element.attr("href")
                            val domain = Uri.parse(linkUrl).host?.replace("www.", "") ?: ""

                            if (isDomainSafe(domain)) {
                                val btn = Button(this@MainActivity).apply {
                                    text = "ŹRÓDŁO: $domain\n${element.text()}"
                                    setOnClickListener { 
                                        Toast.makeText(context, "Analiza strumienia: $domain", Toast.LENGTH_LONG).show()
                                    }
                                }
                                listContainer.addView(btn)
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this@MainActivity, "Błąd sieci", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        root.addView(input); root.addView(btnSearch); root.addView(scroll)
        setContentView(root)
    }
    companion object { init { System.loadLibrary("videoscout") } }
}
