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
import com.app.engine.Scouter

class MainActivity : AppCompatActivity() {
    private var engine: WebView? = null
    private var progress: ProgressBar? = null
    private var monitor: TextView? = null
    private val scouter = Scouter()
    private val activeFilters = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            setPadding(20, 20, 20, 20)
        }

        val input = EditText(this).apply {
            hint = "Tytuł filmu..."
            setTextColor(Color.WHITE)
        }

        val btn = Button(this).apply {
            text = "URUCHOM SILNIK"
            setBackgroundColor(Color.parseColor("#BB86FC"))
        }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
        }
        
        monitor = TextView(this).apply { 
            text = "Status: Gotowy"; setTextColor(Color.GREEN) 
        }

        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"
            
            addJavascriptInterface(object {
                @JavascriptInterface
                fun sendResults(data: String) {
                    runOnUiThread {
                        updateUI(scouter.parseJson(data), resultsArea)
                    }
                }
            }, "AndroidInterface")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    this@MainActivity.monitor?.text = "Skanowanie głębokie..."
                    view?.evaluateJavascript(scouter.generateDiscoveryScript(), null)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                // Zmieniamy na Google, bo DuckDuckGo HTML częściej blokuje boty
                val query = URLEncoder.encode("$q lektor pl", "UTF-8")
                engine?.loadUrl("https://www.google.com/search?q=" + query)
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun updateUI(data: List<Pair<String, Boolean>>, container: LinearLayout) {
        progress?.visibility = View.GONE
        if(data.isEmpty()) {
            monitor?.text = "Brak linków w kodzie strony."
            return
        }
        
        monitor?.text = "Znaleziono ${data.size} potencjalnych źródeł"
        
        data.toSet().forEach { (url, isVideo) ->
            if (!url.contains("google") && !url.contains("gstatic")) {
                val b = Button(this).apply {
                    val domain = Uri.parse(url).host?.replace("www.", "")?.uppercase() ?: "LINK"
                    text = "[$domain] OTWÓRZ"
                    setTextColor(if(isVideo) Color.CYAN else Color.WHITE)
                    setBackgroundColor(Color.parseColor("#1E1E1E"))
                    setOnClickListener { 
                        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
                container.addView(b)
            }
        }
    }
}
