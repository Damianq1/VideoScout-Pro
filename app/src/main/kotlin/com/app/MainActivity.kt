package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var engine: WebView
    private val queue = mutableListOf<String>()
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(20, 20, 20, 20)
        }

        val input = EditText(this).apply { hint = "Tytuł..."; setTextColor(Color.WHITE) }
        val runBtn = Button(this).apply { text = "START ENGINE" }
        val console = TextView(this).apply { text = "Logs..."; setTextColor(Color.GREEN); textSize = 12f }
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(results) }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    console.text = "Processing: $url"
                    
                    // Skrypt wyciągający tylko "mięso" (streamy lub linki do podstron z filmem)
                    view?.evaluateJavascript("""
                        (function() {
                            let data = [];
                            // Szukaj playerów
                            document.querySelectorAll('iframe, video, embed').forEach(v => {
                                if(v.src && v.src.includes('http')) data.push('STREAM|||' + v.src);
                            });
                            // Szukaj linków, które wyglądają na podstrony z filmem
                            document.querySelectorAll('a').forEach(a => {
                                if(a.href.match(/\/(film|v|video|movie|watch)\//)) data.push('LINK|||' + a.href + '|||' + a.innerText);
                            });
                            return data;
                        })();
                    """.trimIndent()) { valStr ->
                        processFoundData(valStr, results, console)
                    }
                }
            }
        }

        runBtn.setOnClickListener {
            results.removeAllViews()
            queue.clear()
            val q = input.text.toString()
            if(q.isEmpty()) return@setOnClickListener
            // Start od DuckDuckGo (skauting)
            engine.loadUrl("https://html.duckduckgo.com/html/?q=${Uri.encode(q + " lektor pl")}")
        }

        root.addView(input); root.addView(runBtn); root.addView(console); root.addView(scroll)
        setContentView(root)
    }

    private fun processFoundData(data: String?, container: LinearLayout, log: TextView) {
        val clean = data?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        val items = clean.split(",")
        
        items.forEach { item ->
            val parts = item.split("|||")
            if (parts[0] == "STREAM") {
                addResultButton(container, "ODTWÓRZ: ${parts[1].take(40)}...", parts[1], Color.GREEN)
            } else if (parts[0] == "LINK" && queue.size < 5) {
                // Jeśli to link do strony, dodaj do kolejki "do sprawdzenia" automatycznie
                val url = parts[1]
                if (!queue.contains(url)) {
                    queue.add(url)
                    log.text = "Queueing: $url"
                    // Automatyczne przejście głębiej po 3 sekundach
                    container.postDelayed({ engine.loadUrl(url) }, 3000)
                }
            }
        }
    }

    private fun addResultButton(container: LinearLayout, label: String, url: String, color: Int) {
        runOnUiThread {
            val b = Button(this).apply {
                text = label
                setTextColor(color)
                setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) }
            }
            container.addView(b)
        }
    }
}
