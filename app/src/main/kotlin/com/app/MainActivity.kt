package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private lateinit var phantomView: WebView
    private val sources = listOf("filman.cc", "vizjer.site", "zaluknij.cc", "iitv.info")
    private var currentMovieQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł filmu..."; setTextColor(Color.CYAN) }
        val btn = Button(this).apply { text = "GŁĘBOKIE SKANOWANIE (ULTRA STEALTH)" }
        val log = TextView(this).apply { text = "Status: Gotowy (Chrome 133 Engine)"; setTextColor(Color.GREEN) }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        phantomView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                // AKTUALIZACJA: Najnowszy User-Agent na rok 2026
                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.6875.122 Mobile Safari/537.36"
            }

            webViewClient = object : WebViewClient() {
                // Udajemy, że jesteśmy prawdziwą przeglądarką wysyłając nowoczesne nagłówki Client Hints
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    request?.requestHeaders?.put("Sec-CH-UA", "\"Not(A:Brand\";v=\"99\", \"Google Chrome\";v=\"133\", \"Chromium\";v=\"133\"")
                    request?.requestHeaders?.put("Sec-CH-UA-Mobile", "?1")
                    request?.requestHeaders?.put("Sec-CH-UA-Platform", "\"Android\"")
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    log.text = "Analizuję źródło: $url"
                    
                    // Skrypt klikający "Ignoruj" lub "Akceptuj" na popupach blokujących
                    view?.evaluateJavascript(
                        "(function() { " +
                        "  var textToClick = ['ignoruj', 'akceptuj', 'rozumiem', 'zgadzam']; " +
                        "  var btns = document.querySelectorAll('button, a, span'); " +
                        "  btns.forEach(function(b) { " +
                        "    textToClick.forEach(function(t) { " +
                        "      if(b.innerText.toLowerCase().indexOf(t) > -1) b.click(); " +
                        "    }); " +
                        "  }); " +
                        "})();"
                    ) {}

                    view?.postDelayed({
                        view.evaluateJavascript(
                            "(function() { " +
                            "  var res = []; " +
                            "  var items = document.querySelectorAll('a[href*=\"/film/\"], a[href*=\"/v/\"], .title a'); " +
                            "  items.forEach(function(i) { res.push(i.innerText + '|||' + i.href); }); " +
                            "  return res; " +
                            "})();"
                        ) { value ->
                            if (value != null && value != "[]" && value != "null") {
                                parseResults(value, resultsArea)
                            } else {
                                // Jeśli pusto, a jesteśmy na stronie logowania/blokady - pokaż WebView
                                runOnUiThread { phantomView.visibility = android.view.View.VISIBLE }
                            }
                        }
                    }, 2500)
                }
            }
        }

        phantomView.layoutParams = RelativeLayout.LayoutParams(-1, 900).apply { addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) }
        phantomView.visibility = android.view.View.GONE

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            currentMovieQuery = input.text.toString()
            phantomView.visibility = android.view.View.GONE
            if(currentMovieQuery.isNotEmpty()) phantomView.loadUrl("https://${sources[0]}/szukaj/${currentMovieQuery}")
        }

        ui.addView(input); ui.addView(btn); ui.addView(log); ui.addView(scroll)
        root.addView(ui); root.addView(phantomView)
        setContentView(root)
    }

    private fun parseResults(value: String, container: LinearLayout) {
        val clean = value.replace("[", "").replace("]", "").replace("\"", "")
        val items = clean.split(",")
        runOnUiThread {
            items.forEach { item ->
                val parts = item.split("|||")
                if (parts.size == 2 && parts[0].length > 2) {
                    val b = Button(this).apply {
                        text = "FILM: ${parts[0].trim()}"
                        setOnClickListener { 
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(parts[1])))
                        }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
