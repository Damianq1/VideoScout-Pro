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
        
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#121212"))
        root.setPadding(30, 30, 30, 30)

        val input = EditText(this)
        input.hint = "Wpisz tytuł..."
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(Color.LTGRAY)
        input.setBackgroundColor(Color.parseColor("#1E1E1E"))
        
        val btn = Button(this)
        btn.text = "SKANUJ ŹRÓDŁA"
        btn.setBackgroundColor(Color.parseColor("#BB86FC"))
        btn.setTextColor(Color.BLACK)
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress?.visibility = View.GONE
        
        monitor = TextView(this)
        monitor?.text = "System gotowy"
        monitor?.setTextColor(Color.parseColor("#03DAC6"))

        val resultsArea = LinearLayout(this)
        resultsArea.orientation = LinearLayout.VERTICAL
        
        val scroll = ScrollView(this)
        val scrollParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        scroll.layoutParams = scrollParams
        scroll.addView(resultsArea)

        engine = WebView(this)
        val ws = engine?.settings
        ws?.javaScriptEnabled = true
        ws?.domStorageEnabled = true
        ws?.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
        
        engine?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                // Linia 66: Używamy jawnego rzutowania i bezpośredniego dostępu
                this@MainActivity.progress?.visibility = android.view.View.VISIBLE
                this@MainActivity.monitor?.text = "Analiza: " + (url?.take(30) ?: "")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                this@MainActivity.progress?.visibility = android.view.View.GONE
                
                view?.evaluateJavascript("(function(){ " +
                    "let l=[]; document.querySelectorAll('a,iframe,video').forEach(x=>{ " +
                    "let s=x.href||x.src; if(s && s.length > 10) l.push(s); " +
                    "}); return l; " +
                    "})();") { data ->
                    displayCleanResults(data, resultsArea)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                val query = URLEncoder.encode(q + " lektor pl", "UTF-8")
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + query)
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun displayCleanResults(data: String?, container: LinearLayout) {
        if (data == null || data == "null") return
        val rawLinks = data.replace("[", "").replace("]", "").replace("\"", "").split(",")
        val uniqueLinks = rawLinks.map { it.trim() }.filter { it.startsWith("http") }.toSet()
        
        runOnUiThread {
            uniqueLinks.forEach { link ->
                if (link.contains("ekino") || link.contains("dailymotion") || link.contains("v=") || link.contains("movie")) {
                    val b = Button(this)
                    val label = link.split("/").lastOrNull { it.isNotEmpty() }?.take(25) ?: "ŹRÓDŁO"
                    b.text = "ODTWÓRZ: " + label
                    b.setBackgroundColor(Color.parseColor("#2C2C2C"))
                    b.setTextColor(Color.parseColor("#03DAC6"))
                    
                    val bParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    bParams.setMargins(0, 8, 0, 8)
                    b.layoutParams = bParams
                    
                    b.setOnClickListener { 
                        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link))) 
                    }
                    container.addView(b)
                }
            }
        }
    }
}
