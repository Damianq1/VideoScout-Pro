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
    private external fun isDomainSafe(domain: String): Boolean

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = RelativeLayout(this).apply {
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val input = EditText(this).apply {
            hint = "Wpisz tytuł (szukam na CDA/Vider)"
            setTextColor(Color.WHITE)
        }

        val btnSearch = Button(this).apply { text = "URUCHOM SKANER (STREAMS ONLY)" }
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        // WebView do rozwiązywania Captcha/Tokenów
        webView = WebView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 400).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            visibility = android.view.View.GONE
            settings.javaScriptEnabled = true
        }

        btnSearch.setOnClickListener {
            val tytul = input.text.toString()
            listContainer.removeAllViews()
            webView.visibility = android.view.View.GONE

            thread {
                try {
                    // Szukamy tylko w konkretnych źródłach
                    val query = "site:cda.pl $tytul | site:vider.info $tytul"
                    val url = "https://duckduckgo.com/html/?q=" + Uri.encode(query)
                    
                    val doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .get()

                    val links = doc.select(".result__a")

                    runOnUiThread {
                        if (links.isEmpty()) {
                            Toast.makeText(this@MainActivity, "Wymagana weryfikacja bota. Ładowanie WebView...", Toast.LENGTH_LONG).show()
                            webView.visibility = android.view.View.VISIBLE
                            webView.loadUrl(url)
                        }
                        
                        links.forEach { element ->
                            val linkUrl = element.attr("href")
                            val btn = Button(this@MainActivity).apply {
                                text = "WYKRYTO: ${element.text()}"
                                setOnClickListener { analyzeWithWebView(linkUrl) }
                            }
                            listContainer.addView(btn)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { 
                        webView.visibility = android.view.View.VISIBLE
                        webView.loadUrl("https://duckduckgo.com/html/?q=" + Uri.encode(tytul))
                    }
                }
            }
        }

        mainLayout.addView(input)
        mainLayout.addView(btnSearch)
        mainLayout.addView(scroll)
        root.addView(mainLayout)
        root.addView(webView)
        setContentView(root)
    }

    private fun analyzeWithWebView(url: String) {
        // Zamiast bota, używamy WebView by udawać 100% człowieka
        webView.visibility = android.view.View.VISIBLE
        webView.loadUrl(url)
        Toast.makeText(this, "Przechodzę do źródła...", Toast.LENGTH_SHORT).show()
    }

    companion object { init { System.loadLibrary("videoscout") } }
}
