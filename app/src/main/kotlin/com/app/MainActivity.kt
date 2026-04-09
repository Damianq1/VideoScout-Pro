package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private external fun isBlacklisted(url: String): Boolean
    private external fun addToBlacklist(domain: String)
    
    private lateinit var phantomView: WebView
    private val sources = mutableListOf("vizjer.site", "zaluknij.cc", "iitv.info", "ekino-tv.pl")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł..."; setTextColor(Color.CYAN) }
        val btnSearch = Button(this).apply { text = "SKANUJ (BEZ ŚMIECI)" }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        phantomView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                // KLUCZOWE: Blokowanie zanim strona się załaduje
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    if (isBlacklisted(url)) {
                        runOnUiThread { Toast.makeText(this@MainActivity, "Zablokowano: $url", Toast.LENGTH_SHORT).show() }
                        return true // Blokuje przejście
                    }
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript("(function() { " +
                        "var res = []; " +
                        "document.querySelectorAll('a[href*=\"/film/\"], a[href*=\"/v/\"]').forEach(a => res.push(a.innerText + '|||' + a.href)); " +
                        "return res; " +
                    "})();") { value ->
                        if (value != "[]" && value != null) parseResults(value, resultsArea)
                    }
                }
            }
        }

        btnSearch.setOnClickListener {
            resultsArea.removeAllViews()
            val query = input.text.toString()
            sources.forEach { s -> if(!isBlacklisted(s)) phantomView.loadUrl("https://$s/szukaj/$query") }
        }

        // Przycisk do szybkiego czyszczenia listy i dodawania do czarnej listy
        val btnBlacklist = Button(this).apply {
            text = "DODAJ OSTATNIĄ DO CZARNEJ LISTY"
            setOnClickListener {
                val currentUrl = phantomView.url ?: ""
                if (currentUrl.isNotEmpty()) {
                    addToBlacklist(currentUrl)
                    resultsArea.removeAllViews()
                    Toast.makeText(this@MainActivity, "Domena wycięta!", Toast.LENGTH_LONG).show()
                }
            }
        }

        ui.addView(input); ui.addView(btnSearch); ui.addView(btnBlacklist); ui.addView(scroll)
        root.addView(ui)
        setContentView(root)
    }

    private fun parseResults(value: String, container: LinearLayout) {
        val items = value.replace("[", "").replace("]", "").replace("\"", "").split(",")
        runOnUiThread {
            items.forEach { item ->
                val parts = item.split("|||")
                if (parts.size == 2 && !isBlacklisted(parts[1])) {
                    container.addView(Button(this).apply {
                        text = "FILM: ${parts[0]}"
                        setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(parts[1]))) }
                    })
                }
            }
        }
    }

    companion object { init { System.loadLibrary("videoscout") } }
}
