package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import org.jsoup.Jsoup
import kotlin.concurrent.thread
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.parseColor("#121212")) }
        val mainLayout = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40) 
        }

        val input = EditText(this).apply {
            hint = "Wpisz tytuł (CDA / VIDER)"
            setTextColor(Color.WHITE)
        }

        val btnSearch = Button(this).apply { text = "GŁĘBOKIE SKANOWANIE BAZ" }
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        webView = WebView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 500).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            settings.javaScriptEnabled = true
            visibility = android.view.View.GONE
        }

        btnSearch.setOnClickListener {
            val query = input.text.toString()
            listContainer.removeAllViews()
            
            thread {
                val results = mutableListOf<Pair<String, String>>()
                
                // 1. Próba CDA (Bezpośrednio)
                try {
                    val cdaUrl = "https://www.cda.pl/info/${Uri.encode(query)}"
                    val doc = Jsoup.connect(cdaUrl).userAgent("Mozilla/5.0").get()
                    doc.select("a.hd-film-link").forEach { 
                        results.add(it.text() to "https://www.cda.pl" + it.attr("href"))
                    }
                } catch(e: Exception) {}

                // 2. Próba VIDER (Bezpośrednio)
                try {
                    val viderUrl = "https://vider.info/szukaj?q=${Uri.encode(query)}"
                    val doc = Jsoup.connect(viderUrl).userAgent("Mozilla/5.0").get()
                    doc.select(".video-title a").forEach {
                        results.add(it.text() to it.attr("href"))
                    }
                } catch(e: Exception) {}

                runOnUiThread {
                    if (results.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Brak wyników. Przełączam na tryb ręczny...", Toast.LENGTH_SHORT).show()
                        webView.visibility = android.view.View.VISIBLE
                        webView.loadUrl("https://www.cda.pl/info/${Uri.encode(query)}")
                    }
                    results.forEach { (name, link) ->
                        val btn = Button(this@MainActivity).apply {
                            text = "FILM: $name"
                            setOnClickListener { 
                                webView.visibility = android.view.View.VISIBLE
                                webView.loadUrl(link)
                            }
                        }
                        listContainer.addView(btn)
                    }
                }
            }
        }

        mainLayout.addView(input); mainLayout.addView(btnSearch); mainLayout.addView(scroll)
        root.addView(mainLayout); root.addView(webView)
        setContentView(root)
    }
}
