package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri
import android.view.View
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private var engine: WebView? = null
    private var progress: ProgressBar? = null
    private var monitor: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
        }

        val input = EditText(this).apply { 
            hint = "Szukaj filmu (np. Mavka)..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }
        val btn = Button(this).apply { text = "SKANUJ I FILTRUJ WIDEO" }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
        }
        
        monitor = TextView(this).apply { 
            text = "Status: Gotowy"; setTextColor(Color.CYAN); textSize = 11f 
        }

        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { 
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progress?.visibility = View.GONE
                    monitor?.text = "Analiza głęboka: " + (url?.take(30))
                    
                    // JS szukający wideo z filtrem długości (> 60 min)
                    val filterJS = """
                        (function() {
                            let found = [];
                            // 1. Szukaj w tagach video
                            document.querySelectorAll('video').forEach(v => {
                                let duration = v.duration / 60; // w minutach
                                if(v.src && (duration > 60 || !duration)) {
                                    found.push('VIDEO|||' + v.src + '|||Min: ' + Math.round(duration));
                                }
                            });
                            // 2. Szukaj w iframe'ach (odtwarzacze zewnętrzne)
                            document.querySelectorAll('iframe').forEach(f => {
                                if(f.src && f.src.includes('http') && !f.src.includes('ads')) {
                                    found.push('PLAYER|||' + f.src + '|||Embed');
                                }
                            });
                            return found;
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(filterJS) { data ->
                        processFilteredResults(data, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                val encoded = URLEncoder.encode(q + " lektor pl", "UTF-8")
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + encoded)
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun processFilteredResults(data: String?, container: LinearLayout) {
        if (data == null || data == "null" || data == "[]") return
        val items = data.replace("[", "").replace("]", "").replace("\"", "").split(",")
        
        runOnUiThread {
            items.forEach { item ->
                val p = item.split("|||")
                if (p.size >= 2) {
                    val type = p[0]
                    val url = p[1].trim()
                    val info = if(p.size > 2) p[2] else ""

                    // Dodajemy przycisk tylko jeśli to nie jest śmieć
                    if (!url.contains("google") && !url.contains("facebook")) {
                        val b = Button(this).apply {
                            text = "[$type] $info - " + url.takeLast(25)
                            setTextColor(if(type == "VIDEO") Color.GREEN else Color.YELLOW)
                            setOnClickListener { 
                                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) 
                            }
                        }
                        container.addView(b)
                    }
                }
            }
        }
    }
}
