package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var agentView: WebView
    private val discoveredUrls = mutableListOf<String>()
    private var scanIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł..."; setTextColor(Color.WHITE) }
        val btn = Button(this).apply { text = "AUTO-SKANER (PYTHON LOGIC)" }
        val log = TextView(this).apply { text = "Status: Czekam"; setTextColor(Color.GREEN) }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        agentView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (url?.contains("duckduckgo.com") == true) {
                        // FAZA 1: Pobieramy linki i filtrujemy śmieci
                        view?.evaluateJavascript("(function() { " +
                            "  var res = []; " +
                            "  document.querySelectorAll('.result__a').forEach(l => { " +
                            "    if(!l.href.includes('google') && !l.href.includes('duckduckgo')) res.push(l.href); " +
                            "  }); " +
                            "  return res; " +
                        "})();") { value ->
                            val urls = value.replace("[", "").replace("]", "").replace("\"", "").split(",")
                            discoveredUrls.clear()
                            discoveredUrls.addAll(urls.filter { it.length > 10 })
                            if(discoveredUrls.isNotEmpty()) nextStep(log)
                        }
                    } else {
                        // FAZA 2: Jesteśmy na stronie źródłowej - szukamy odtwarzacza
                        log.text = "Analiza głęboka: $url"
                        view?.evaluateJavascript("(function() { " +
                            "  var players = []; " +
                            "  document.querySelectorAll('iframe, video, source, a[href*=\".mp4\"]').forEach(i => { " +
                            "    players.push((i.title || i.innerText || 'Player') + '|||' + (i.src || i.href)); " +
                            "  }); " +
                            "  return players; " +
                        "})();") { res -> parseFinalLinks(res, resultsArea, log) }
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            scanIndex = 0
            val query = "${input.text} lektor pl -zwiastun"
            log.text = "Skauting w toku..."
            agentView.loadUrl("https://html.duckduckgo.com/html/?q=${Uri.encode(query)}")
        }

        ui.addView(input); ui.addView(btn); ui.addView(log); ui.addView(scroll)
        root.addView(ui)
        setContentView(root)
    }

    private fun nextStep(log: TextView) {
        if (scanIndex < discoveredUrls.size && scanIndex < 5) {
            log.text = "Wchodzę na: ${discoveredUrls[scanIndex]}"
            agentView.loadUrl(discoveredUrls[scanIndex])
            scanIndex++
        }
    }

    private fun parseFinalLinks(value: String?, container: LinearLayout, log: TextView) {
        val clean = value?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        if (clean.length < 5) { nextStep(log); return } // Nic nie ma, idź do nast. strony

        clean.split(",").forEach { item ->
            val parts = item.split("|||")
            if (parts.size == 2 && parts[1].startsWith("http")) {
                runOnUiThread {
                    val b = Button(this).apply {
                        text = "GOTOWY STREAM: ${parts[0].take(25)}"
                        setTextColor(Color.GREEN)
                        setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parts[1]))) }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
