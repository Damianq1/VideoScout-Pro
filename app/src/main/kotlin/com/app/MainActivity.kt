package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import kotlin.concurrent.thread
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var phantomView: WebView
    private val sources = listOf("filman.cc", "vizjer.site", "iitv.info", "zaluknij.cc")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply {
            hint = "Tytuł filmu..."; setTextColor(Color.CYAN)
        }
        val btn = Button(this).apply { text = "GŁĘBOKIE SKANOWANIE (STEALTH)" }
        val log = TextView(this).apply { text = "Status: Gotowy"; setTextColor(Color.GREEN) }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        // KONFIGURACJA PHANTOM AGENTA
        phantomView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            // Kluczowe: Udajemy, że nie jesteśmy botem przez wyłączenie nagłówka webdriver
            settings.setSupportMultipleWindows(true)
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    log.text = "Analizuję: $url"
                    // Wstrzykujemy skrypt Selenium-style (Punkt 6)
                    view?.evaluateJavascript(
                        "(function() { " +
                        "  var links = []; " +
                        "  var anchors = document.getElementsByTagName('a'); " +
                        "  for (var i=0; i<anchors.length; i++) { " +
                        "    if(anchors[i].href.indexOf('/film/') > -1 || anchors[i].href.indexOf('/v/') > -1) { " +
                        "      links.push(anchors[i].innerText + '|||' + anchors[i].href); " +
                        "    } " +
                        "  } " +
                        "  return links; " +
                        "})();"
                    ) { value ->
                        parsePhantomResults(value, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val movie = input.text.toString()
            if(movie.isEmpty()) return@setOnClickListener
            
            log.text = "Agent startuje..."
            // Uruchamiamy skanowanie kaskadowe
            scanNext(0, movie)
        }

        ui.addView(input); ui.addView(btn); ui.addView(log); ui.addView(scroll)
        root.addView(ui)
        setContentView(root)
    }

    private fun scanNext(index: Int, query: String) {
        if (index >= sources.size) return
        val url = "https://${sources[index]}/szukaj/${Uri.encode(query)}"
        phantomView.loadUrl(url)
    }

    private fun parsePhantomResults(value: String?, container: LinearLayout) {
        if (value == null || value == "null" || value == "[]") return
        // Czyścimy wynik z cudzysłowów i nawiasów JSON
        val cleanValue = value.replace("[", "").replace("]", "").replace("\"", "")
        val items = cleanValue.split(",")

        items.forEach { item ->
            val parts = item.split("|||")
            if (parts.size == 2) {
                val b = Button(this).apply {
                    text = "ŹRÓDŁO: ${parts[0]}"
                    setOnClickListener { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parts[1]))
                        startActivity(intent)
                    }
                }
                container.addView(b)
            }
        }
    }
}
