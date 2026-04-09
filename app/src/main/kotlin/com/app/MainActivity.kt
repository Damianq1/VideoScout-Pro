package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var agent: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK) 
        }

        val input = EditText(this).apply { hint = "Tytuł z Pythona..."; setTextColor(Color.WHITE) }
        val btn = Button(this).apply { text = "URUCHOM ANALIZATOR (PYTHON LOGIC)" }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        agent = WebView(this).apply {
            settings.javaScriptEnabled = true
            // Kluczowe: Udajemy desktopowego Firefoxa, by nie dostać blokady mobilnej
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Implementacja Twojej "Fazy 2" z Pythona bezpośrednio w JS
                    val scraperJS = """
                        (function() {
                            let links = [];
                            // Szukamy linków, które nie są śmieciami (Twój filtr z Pythona)
                            document.querySelectorAll('a').forEach(a => {
                                let href = a.href;
                                if(href.match(/filman|ekino|vizjer|zaluknij|vider|dailymotion/) || href.includes('video')) {
                                    links.push(a.innerText + '|||' + href);
                                }
                            });
                            return links;
                        })();
                    """.trimIndent()
                    
                    view?.evaluateJavascript(scraperJS) { res ->
                        displaySmartResults(res, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val query = Uri.encode(input.text.toString() + " lektor pl")
            // Używamy wersji HTML DuckDuckGo, która jest lżejsza dla skryptów
            agent.loadUrl("https://html.duckduckgo.com/html/?q=$query")
        }

        root.addView(input); root.addView(btn); root.addView(resultsArea)
        setContentView(root)
    }

    private fun displaySmartResults(data: String?, container: LinearLayout) {
        val clean = data?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        clean.split(",").forEach { item ->
            val p = item.split("|||")
            if (p.size >= 2) {
                runOnUiThread {
                    val b = Button(this).apply {
                        text = "ZNALEZIONO: " + p[0].take(30)
                        setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(p[1]))) }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
