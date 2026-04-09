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
    // Definiujemy jako nullable, aby uniknąć problemów z lateinit
    private var engine: WebView? = null
    private var progress: ProgressBar? = null
    private var monitor: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(16, 16, 16, 16)

        val input = EditText(this)
        input.hint = "Szukaj filmu..."
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(Color.GRAY)
        
        val btn = Button(this)
        btn.text = "START SKANER"
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress?.visibility = View.GONE
        
        monitor = TextView(this)
        monitor?.text = "Gotowy"
        monitor?.setTextColor(Color.GREEN)

        val resultsArea = LinearLayout(this)
        resultsArea.orientation = LinearLayout.VERTICAL
        
        val scroll = ScrollView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        scroll.layoutParams = params
        scroll.addView(resultsArea)

        engine = WebView(this)
        val settings = engine?.settings
        settings?.javaScriptEnabled = true
        settings?.domStorageEnabled = true
        settings?.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0"
        
        engine?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progress?.visibility = View.VISIBLE
                monitor?.text = "Skanuję: " + (url?.take(30) ?: "")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress?.visibility = View.GONE
                monitor?.append("\n[OK] Szukam linków...")
                
                view?.evaluateJavascript("(function(){ " +
                    "var l=[]; document.querySelectorAll('a,iframe,video').forEach(x=>{ " +
                    "var s=x.href||x.src; if(s&&s.includes('http')) l.push(s); " +
                    "}); return l; " +
                    "})();") { data ->
                    processFinalLinks(data, resultsArea)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val query = input.text.toString()
            if(query.isNotEmpty()) {
                val encoded = URLEncoder.encode(query + " lektor pl", "UTF-8")
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + encoded)
            }
        }

        root.addView(input)
        root.addView(btn)
        root.addView(progress)
        root.addView(monitor)
        root.addView(scroll)
        setContentView(root)
    }

    private fun processFinalLinks(data: String?, container: LinearLayout) {
        if (data == null || data == "null") return
        val items = data.replace("[", "").replace("]", "").replace("\"", "").split(",")
        
        runOnUiThread {
            items.forEach { link ->
                val l = link.trim()
                if (l.contains("ekino") || l.contains("dailymotion") || l.contains("film") || l.contains("v=")) {
                    val b = Button(this)
                    b.text = "LINK: " + l.takeLast(30)
                    b.setTextColor(Color.CYAN)
                    b.setOnClickListener { 
                        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(l))) 
                    }
                    container.addView(b)
                }
            }
        }
    }
}
