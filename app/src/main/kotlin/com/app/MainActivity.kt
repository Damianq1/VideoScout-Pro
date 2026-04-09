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
    private external fun getUpdateUrl(): String
    private lateinit var agentView: WebView
    private val activeSources = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#050505"))
            setPadding(30,30,30,30)
        }

        val input = EditText(this).apply { 
            hint = "Tytuł filmu..."; setTextColor(Color.WHITE) 
        }
        val btnSearch = Button(this).apply { text = "URUCHOM AGENTA (STEALTH)" }
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(results) }

        // Nasz "Selenium" Agent
        agentView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Mobile Safari/537.36"
            visibility = android.view.View.GONE // Niewidoczny dla użytkownika
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Po załadowaniu strony "wstrzykujemy" skrypt wyciągający linki
                    view?.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                        analyzeHtml(html, results)
                    }
                }
            }
        }

        // Auto-aktualizacja źródeł przy starcie
        updateSources()

        btnSearch.setOnClickListener {
            results.removeAllViews()
            val movie = input.text.toString()
            activeSources.forEach { domain ->
                val searchUrl = "https://$domain/szukaj/${Uri.encode(movie)}"
                agentView.loadUrl(searchUrl)
            }
        }

        root.addView(input); root.addView(btnSearch); root.addView(scroll); root.addView(agentView)
        setContentView(root)
    }

    private fun updateSources() {
        thread {
            try {
                // Symulacja pobierania nowej listy źródeł
                val freshList = Jsoup.connect("https://pastebin.com/raw/TwojaLista").get().text()
                activeSources.clear()
                activeSources.addAll(freshList.split(","))
            } catch(e: Exception) {
                // Fallback do Twoich sprawdzonych linków
                activeSources.addAll(listOf("filman.cc", "vizjer.site", "zaluknij.cc", "iitv.info"))
            }
        }
    }

    private fun analyzeHtml(html: String?, container: LinearLayout) {
        if (html == null) return
        val doc = Jsoup.parse(html)
        val links = doc.select("a[href*=/film/], a[href*=/v/]")
        
        runOnUiThread {
            links.take(3).forEach { link ->
                val b = Button(this).apply {
                    text = "ZNALEZIONO: ${link.text().take(25)}"
                    setOnClickListener { 
                        // Otwieramy w widocznym WebView, by użytkownik mógł obejrzeć
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link.attr("abs:href")))
                        startActivity(intent)
                    }
                }
                container.addView(b)
            }
        }
    }

    companion object { init { System.loadLibrary("videoscout") } }
}
