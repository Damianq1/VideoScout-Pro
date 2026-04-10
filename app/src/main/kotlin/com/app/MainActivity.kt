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
    private var progress: ProgressBar? = null
    private var monitor: TextView? = null
    private val scouter = Scouter()

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
            text = "INTELIGENTNY SKAUTING"
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

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) Chrome/110.0.0.0 Mobile"
            
            addJavascriptInterface(object {
                @JavascriptInterface
                fun sendResults(data: String) {
                    runOnUiThread {
                        val parsed = scouter.parseJson(data)
                        updateUI(parsed, resultsArea)
                    }
                }
            }, "AndroidInterface")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    monitor?.text = "Analiza kontekstu filmowego..."
                    view?.evaluateJavascript(scouter.generateDiscoveryScript(), null)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                val query = URLEncoder.encode("$q film online", "UTF-8")
                webView.loadUrl("https://www.google.com/search?q=" + query)
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun updateUI(data: List<Pair<String, String>>, container: LinearLayout) {
        progress?.visibility = View.GONE
        if(data.isEmpty()) {
            monitor?.text = "Nie widzę nic ciekawego na tej stronie."
            return
        }
        
        // Unikalność linków
        data.distinctBy { it.first }.forEach { (url, title) ->
            if (!url.contains("google") && !url.contains("gstatic")) {
                val domain = Uri.parse(url).host?.replace("www.", "")?.uppercase() ?: "INFO"
                val b = Button(this).apply {
                    // Jeśli skaner znalazł tekst (np. "Lektor PL"), wyświetlamy go
                    val label = if (title.length > 5) title.uppercase() else "SPRAWDŹ ŹRÓDŁO"
                    text = "[$domain] $label"
                    
                    setTextColor(Color.CYAN)
                    setBackgroundColor(Color.parseColor("#1E1E1E"))
                    
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 5, 0, 5)
                    layoutParams = params

                    setOnClickListener { 
                        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
                container.addView(b)
            }
        }
        monitor?.text = "Znaleziono ${container.childCount} inteligentnych odnośników."
    }
}
