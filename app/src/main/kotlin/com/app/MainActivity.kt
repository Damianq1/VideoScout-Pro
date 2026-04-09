package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var phantomView: WebView
    private val sources = listOf("filman.cc", "vizjer.site", "iitv.info", "zaluknij.cc")
    private var currentSourceIndex = 0
    private var currentMovieQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł filmu..."; setTextColor(Color.CYAN) }
        val btn = Button(this).apply { text = "GŁĘBOKIE SKANOWANIE (STEALTH v2)" }
        val log = TextView(this).apply { text = "Status: Gotowy"; setTextColor(Color.GREEN) }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        phantomView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                // Udajemy najnowszego Chrome na Androidzie
                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            }
            
            // Włączamy obsługę ciasteczek (Kluczowe!)
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    log.text = "Analizuję: $url"
                    
                    // 1. Automatycznie klikamy "Akceptuję" na popupach ciasteczek
                    view?.evaluateJavascript(
                        "var btns = document.getElementsByTagName('button'); " +
                        "for(var i=0; i<btns.length; i++) { " +
                        "  if(btns[i].innerText.toLowerCase().indexOf('akceptuj') > -1) btns[i].click(); " +
                        "}"
                    ) {}

                    // 2. Wyciągamy linki po krótkim opóźnieniu (na wypadek renderowania JS)
                    view?.postDelayed({
                        view.evaluateJavascript(
                            "(function() { " +
                            "  var results = []; " +
                            "  var links = document.querySelectorAll('a[href*=\"/film/\"], a[href*=\"/v/\"], a.title'); " +
                            "  links.forEach(function(l) { results.push(l.innerText + '|||' + l.href); }); " +
                            "  return results; " +
                            "})();"
                        ) { value ->
                            if (value != null && value != "[]" && value != "null") {
                                parseResults(value, resultsArea)
                            } else if (url?.contains("logowanie") == true || url?.contains("verify") == true) {
                                // Jeśli strona nas zablokowała, pokazujemy ją użytkownikowi
                                runOnUiThread { 
                                    log.text = "Wymagana interakcja na $url"
                                    view.visibility = android.view.View.VISIBLE 
                                }
                            }
                        }
                    }, 2000)
                }
            }
        }

        // WebView zajmuje dół ekranu tylko gdy potrzebna interakcja
        phantomView.layoutParams = RelativeLayout.LayoutParams(-1, 800).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        }
        phantomView.visibility = android.view.View.GONE

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            currentMovieQuery = input.text.toString()
            if(currentMovieQuery.isEmpty()) return@setOnClickListener
            phantomView.visibility = android.view.View.GONE
            startScan(0)
        }

        ui.addView(input); ui.addView(btn); ui.addView(log); ui.addView(scroll)
        root.addView(ui); root.addView(phantomView)
        setContentView(root)
    }

    private fun startScan(index: Int) {
        if (index >= sources.size) return
        val url = "https://${sources[index]}/szukaj/${Uri.encode(currentMovieQuery)}"
        phantomView.loadUrl(url)
    }

    private fun parseResults(value: String, container: LinearLayout) {
        val items = value.replace("[", "").replace("]", "").replace("\"", "").split(",")
        runOnUiThread {
            items.forEach { item ->
                val parts = item.split("|||")
                if (parts.size == 2 && parts[0].length > 3) {
                    val b = Button(this).apply {
                        text = "LINK: ${parts[0].trim()}"
                        setOnClickListener { 
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parts[1])))
                        }
                    }
                    container.addView(b)
                }
            }
        }
    }
}
