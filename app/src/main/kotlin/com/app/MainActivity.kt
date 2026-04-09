package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var agentView: WebView
    private val forbidden = listOf("google", "youtube", "facebook", "wikipedia", "filmweb", "imdb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł (np. Mavka 2023)"; setTextColor(Color.CYAN) }
        val btn = Button(this).apply { text = "URUCHOM INTELIGENTNY SKAUTING" }
        val status = TextView(this).apply { text = "Gotowy"; setTextColor(Color.GRAY) }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        agentView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // FAZA 1: Skauting linków z wyników wyszukiwania (jak w Twoim Pythonie)
                    if (url?.contains("duckduckgo.com") == true) {
                        status.text = "Skauting: Analiza wyników..."
                        view?.evaluateJavascript("(function() { " +
                            "  var res = []; " +
                            "  var links = document.querySelectorAll('.result__a'); " +
                            "  links.forEach(l => res.push(l.innerText + '|||' + l.href)); " +
                            "  return res; " +
                            "})();") { value -> handleDiscovery(value, resultsArea, status) }
                    } 
                    // FAZA 2: Głęboka analiza konkretnej strony w poszukiwaniu wideo
                    else {
                        status.text = "Analiza głęboka: $url"
                        view?.evaluateJavascript("(function() { " +
                            "  var videos = []; " +
                            "  document.querySelectorAll('a[href*=\"/film/\"], a[href*=\"/v/\"], source, video').forEach(v => { " +
                            "    videos.push(v.innerText || 'LINK WIDEO' + '|||' + (v.href || v.src)); " +
                            "  }); " +
                            "  return videos; " +
                            "})();") { value -> parseVideoLinks(value, resultsArea) }
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val query = "${input.text} lektor pl -zwiastun"
            status.text = "Szukam nowych źródeł..."
            agentView.loadUrl("https://html.duckduckgo.com/html/?q=${Uri.encode(query)}")
        }

        ui.addView(input); ui.addView(btn); ui.addView(status); ui.addView(scroll)
        root.addView(ui)
        setContentView(root)
    }

    private fun handleDiscovery(value: String?, container: LinearLayout, status: TextView) {
        val clean = value?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        val items = clean.split(",")
        items.forEach { item ->
            val parts = item.split("|||")
            if (parts.size == 2) {
                val domain = Uri.parse(parts[1]).host ?: ""
                // Filtracja "gigantów" (jak w Twoim Pythonie)
                if (domain.isNotEmpty() && !forbidden.any { domain.contains(it) }) {
                    runOnUiThread {
                        val b = Button(this).apply {
                            text = "SKANUJ: $domain"
                            setBackgroundColor(Color.parseColor("#1A1A1A"))
                            setOnClickListener { agentView.loadUrl(parts[1]) }
                        }
                        container.addView(b)
                    }
                }
            }
        }
    }

    private fun parseVideoLinks(value: String?, container: LinearLayout) {
        val clean = value?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        val items = clean.split(",")
        runOnUiThread {
            items.forEach { item ->
                val parts = item.split("|||")
                if (parts.size == 2 && parts[1].startsWith("http")) {
                    val b = Button(this).apply {
                        text = "ODTWÓRZ: ${parts[0].take(30)}"
                        setTextColor(Color.GREEN)
                        setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parts[1]))) }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
