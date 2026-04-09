package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private var engine: WebView? = null
    private var progress: ProgressBar? = null
    private var monitor: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212")) // Głębsza czerń
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { 
            hint = "Wpisz tytuł..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(20, 20, 20, 20)
        }
        
        val btn = Button(this).apply { 
            text = "SKANUJ ŹRÓDŁA"
            setBackgroundColor(Color.parseColor("#BB86FC")) // Fioletowy akcent Material
            setTextColor(Color.BLACK)
        }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
            progressDrawable.setTint(Color.parseColor("#03DAC6"))
        }
        
        monitor = TextView(this).apply { 
            text = "System gotowy"; setTextColor(Color.parseColor("#03DAC6")); textSize = 12f 
        }

        val resultsArea = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 0)
        }
        
        val scroll = ScrollView(this).apply { 
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progress?.visibility = View.GONE
                    monitor?.text = "Analiza: " + (url?.take(40))
                    
                    view?.evaluateJavascript("(function(){ " +
                        "let l=[]; document.querySelectorAll('a,iframe,video').forEach(x=>{ " +
                        "let s=x.href||x.src; if(s && s.length > 10) l.push(s); " +
                        "}); return l; " +
                        "})();") { data ->
                        displayCleanResults(data, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(q + " lektor pl", "UTF-8"))
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun displayCleanResults(data: String?, container: LinearLayout) {
        if (data == null || data == "null") return
        
        // Użycie Set eliminuje duplikaty widoczne na zrzucie ekranu
        val rawLinks = data.replace("[", "").replace("]", "").replace("\"", "").split(",")
        val uniqueLinks = rawLinks.map { it.trim() }.filter { it.startsWith("http") }.toSet()
        
        runOnUiThread {
            uniqueLinks.forEach { link ->
                // Filtracja "śmieciowych" linków nawigacyjnych
                if (link.contains("ekino") || link.contains("dailymotion") || link.contains("v=") || link.contains("movie")) {
                    val b = Button(this).apply {
                        // Bardziej czytelna etykieta zamiast hashu
                        val label = link.split("/").lastOrNull { it.isNotEmpty() }?.take(25) ?: "ŹRÓDŁO"
                        text = "ODTWÓRZ: $label"
                        
                        // Kolory przyjazne dla oka: Ciemne tło przycisku, jasny tekst
                        setBackgroundColor(Color.parseColor("#2C2C2C"))
                        setTextColor(Color.parseColor("#03DAC6")) // Turkusowy tekst
                        
                        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 5, 0, 5)
                        layoutParams = params
                        
                        setOnClickListener { 
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link))) 
                        }
                    }
                    container.addView(b)
                }
            }
            if(container.childCount == 0 && uniqueLinks.isNotEmpty()) {
                monitor?.text = "Znaleziono linki, ale nie pasują do filtrów (Ekino/Daily)."
            }
        }
    }
}
