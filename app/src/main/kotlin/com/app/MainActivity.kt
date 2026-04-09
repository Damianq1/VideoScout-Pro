package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var engine: WebView
    private lateinit var progress: ProgressBar
    private lateinit var monitor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
        }

        val input = EditText(this).apply { 
            hint = "Wpisz tytuł filmu..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE) 
        }
        
        val btn = Button(this).apply { text = "URUCHOM SILNIK ANALIZY" }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            // Używamy pełnej ścieżki, żeby uniknąć błędów kompilacji
            visibility = android.view.View.GONE
        }
        
        monitor = TextView(this).apply { 
            text = "Status: Gotowy"; setTextColor(Color.GREEN); textSize = 11f 
        }

        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { 
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea) 
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progress.visibility = android.view.View.VISIBLE
                    monitor.text = "Łączenie z: " + (url?.take(40) ?: "...")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progress.visibility = android.view.View.GONE
                    monitor.append("\n[OK] Analizuję źródło...")
                    
                    val script = """
                        (function() {
                            let results = [];
                            document.querySelectorAll('a, iframe, video').forEach(item => {
                                let link = item.href || item.src;
                                if(link && link.includes('http')) results.push(link);
                            });
                            return results;
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(script) { data ->
                        processLinks(data, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            monitor.text = "Inicjalizacja..."
            val text = input.text.toString()
            if(text.isNotEmpty()) {
                val query = URLEncoder.encode(text + " lektor pl", "UTF-8")
                engine.loadUrl("https://html.duckduckgo.com/html/?q=" + query)
            }
        }

        root.addView(input)
        root.addView(btn)
        root.addView(progress)
        root.addView(monitor)
        root.addView(scroll)
        setContentView(root)
    }

    private fun processLinks(data: String?, container: LinearLayout) {
        if (data == null || data == "null" || data == "[]") return
        val links = data.replace("[", "").replace("]", "").replace("\"", "").split(",")
        
        runOnUiThread {
            links.forEach { rawUrl ->
                val u = rawUrl.trim()
                if (u.contains("ekino") || u.contains("dailymotion") || u.contains("film") || u.contains("v=")) {
                    val b = Button(this).apply {
                        text = "LINK: " + u.takeLast(30)
                        setTextColor(Color.CYAN)
                        setOnClickListener { 
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(u))) 
                        }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
