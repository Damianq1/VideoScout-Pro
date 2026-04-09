package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.view.View
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

        val input = EditText(this).apply { hint = "Tytuł filmu..."; setTextColor(Color.WHITE) }
        val btn = Button(this).apply { text = "URUCHOM SKANER" }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
        }
        
        monitor = TextView(this).apply { 
            text = "System: Gotowy"; setTextColor(Color.GREEN); textSize = 10f 
        }

        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { 
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(results) 
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:115.0) Gecko/20100101 Firefox/115.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progress.visibility = View.VISIBLE
                    monitor.text = "Ładowanie: " + (url?.take(50) ?: "")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progress.visibility = View.GONE
                    monitor.append("\n[OK] Strona załadowana. Skanuję kod...")
                    
                    val scraperJS = """
                        (function() {
                            let found = [];
                            document.querySelectorAll('a, iframe, video').forEach(el => {
                                let link = el.href || el.src;
                                if(link && link.startsWith('http')) found.push(link);
                            });
                            return found;
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(scraperJS) { res ->
                        updateUI(res, results)
                    }
                }
            }
        }

        btn.setOnClickListener {
            results.removeAllViews()
            monitor.text = "Szukam danych..."
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                val encoded = URLEncoder.encode(q + " lektor pl", "UTF-8")
                engine.loadUrl("https://html.duckduckgo.com/html/?q=$encoded")
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun updateUI(data: String?, container: LinearLayout) {
        val clean = data?.replace("[", "")?.replace("]", "")?.replace("\"", "") ?: return
        if (clean.length < 5) return

        clean.split(",").forEach { url ->
            val u = url.trim()
            // Filtrujemy tylko to, co nas interesuje (uproszczona logika bez błędnych ukośników)
            if (u.contains("ekino") || u.contains("dailymotion") || u.contains("v=") || u.contains("film")) {
                runOnUiThread {
                    val b = Button(this).apply {
                        text = "LINK: " + u.takeLast(35)
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
