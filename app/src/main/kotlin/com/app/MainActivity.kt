package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    private lateinit var engine: WebView
    private val queue = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastUrl = ""
    private var stallCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        val input = EditText(this).apply { hint = "Tytuł..."; setTextColor(Color.WHITE) }
        val runBtn = Button(this).apply { text = "FORCE START ENGINE" }
        val console = TextView(this).apply { text = "System IDLE"; setTextColor(Color.GREEN) }
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(results) }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    lastUrl = url ?: ""
                    stallCount = 0
                    injectAnalyzer(results, console)
                }
            }
        }

        runBtn.setOnClickListener {
            results.removeAllViews()
            queue.clear()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                engine.loadUrl("https://html.duckduckgo.com/html/?q=${Uri.encode(q + " lektor pl")}")
                startHeartbeat(results, console)
            }
        }

        root.addView(input); root.addView(runBtn); root.addView(console); root.addView(scroll)
        setContentView(root)
    }

    private fun startHeartbeat(container: LinearLayout, log: TextView) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(object : Runnable {
            override fun run() {
                stallCount++
                log.text = "Heartbeat: Skanowanie... (Stall: $stallCount)"
                
                // Jeśli stoi za długo, wymuś scroll i analizę
                engine.evaluateJavascript("window.scrollTo(0, document.body.scrollHeight);", null)
                injectAnalyzer(container, log)

                // Jeśli system wisi powyżej 15s na jednej stronie, skocz do następnej w kolejce
                if (stallCount > 3 && queue.isNotEmpty()) {
                    val next = queue.removeAt(0)
                    log.text = "Timeout! Przeskok do: $next"
                    engine.loadUrl(next)
                    stallCount = 0
                }
                handler.postDelayed(this, 5000)
            }
        }, 5000)
    }

    private fun injectAnalyzer(container: LinearLayout, log: TextView) {
        engine.evaluateJavascript("""
            (function() {
                let found = [];
                // Wyciągamy streamy bezpośrednie
                document.querySelectorAll('iframe, video, source').forEach(v => {
                    let src = v.src || v.getAttribute('src');
                    if(src && src.startsWith('http')) found.push('STREAM|||' + src);
                });
                // Wyciągamy linki do głębokiej analizy
                document.querySelectorAll('a').forEach(a => {
                    if(a.href.match(/\/(film|v|video|watch|movie)\//) && !a.href.includes('google')) 
                        found.push('LINK|||' + a.href);
                });
                return found;
            })();
        """.trimIndent()) { data ->
            val items = data?.replace("[", "")?.replace("]", "")?.replace("\"", "")?.split(",") ?: return@evaluateJavascript
            items.forEach { item ->
                val p = item.split("|||")
                if(p.size >= 2) {
                    if(p[0] == "STREAM") addResult(container, "WIDEO: " + p[1].take(30), p[1])
                    else if(p[0] == "LINK" && !queue.contains(p[1])) queue.add(p[1])
                }
            }
        }
    }

    private fun addResult(container: LinearLayout, txt: String, url: String) {
        runOnUiThread {
            if (container.findViewWithTag<Button>(url) == null) {
                val b = Button(this).apply {
                    text = txt; tag = url; setTextColor(Color.GREEN)
                    setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) }
                }
                container.addView(b)
            }
        }
    }
}
