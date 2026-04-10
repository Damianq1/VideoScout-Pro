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
    private var monitor: TextView? = null
    private val scouter = Scouter()
    private lateinit var resultsArea: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A0A"))
            setPadding(20, 20, 20, 20)
        }

        val input = EditText(this).apply {
            hint = "Wpisz tytuł..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        val btn = Button(this).apply {
            text = "DEEP SCAN (AGRESYWNY)"
            setBackgroundColor(Color.parseColor("#BB86FC"))
        }
        
        monitor = TextView(this).apply { 
            text = "Status: Gotowy"; setTextColor(Color.CYAN); textSize = 12f 
        }

        resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        val web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            addJavascriptInterface(object {
                @JavascriptInterface
                fun sendResults(data: String) {
                    runOnUiThread {
                        val parsed = scouter.parseJson(data)
                        if(parsed.isNotEmpty()) {
                            monitor?.text = "Znalazłem ${parsed.size} linków!"
                            updateUI(parsed)
                        }
                    }
                }
            }, "AndroidInterface")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript(scouter.generateDiscoveryScript(), null)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                monitor?.text = "Uruchamiam silnik..."
                val query = URLEncoder.encode("$q film online", "UTF-8")
                web.loadUrl("https://www.google.com/search?q=" + query)
            }
        }

        root.addView(input); root.addView(btn); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun updateUI(data: List<Pair<String, String>>) {
        data.forEach { (url, title) ->
            val b = Button(this).apply {
                val domain = Uri.parse(url).host?.replace("www.", "")?.uppercase() ?: "LINK"
                text = "[$domain] ${title.take(20)}"
                setBackgroundColor(Color.parseColor("#1E1E1E"))
                setTextColor(Color.WHITE)
                setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) }
            }
            resultsArea.addView(b)
        }
    }
}
