package com.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.*
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private external fun isBlacklisted(url: String): Boolean
    private lateinit var phantomView: WebView
    // Twoja lista "pewniaków"
    private val mySources = listOf("vizjer.site", "zaluknij.cc", "iitv.info", "ekino-tv.pl", "vider.info")
    private var sourceIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val ui = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply { hint = "Tytuł..."; setTextColor(Color.WHITE) }
        val btn = Button(this).apply { text = "SKANUJ (BEZ ŚMIECI)" }
        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(resultsArea) }

        phantomView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0"
            
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    // Blokujemy tylko jeśli to twardy syf z C++
                    return isBlacklisted(url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Wyciągamy linki: szukamy wszystkiego co ma /film/ lub /wideo/
                    view?.evaluateJavascript("(function() { " +
                        "var found = []; " +
                        "document.querySelectorAll('a').forEach(a => { " +
                        "  if(a.href.match(/\\/(film|wideo|v)\\//)) found.push(a.innerText + '|||' + a.href); " +
                        "}); " +
                        "return found; " +
                    "})();") { value ->
                        if (value != "[]" && value != "null") {
                            parseAndDisplay(value, resultsArea)
                        }
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            sourceIndex = 0
            val movie = input.text.toString()
            if (movie.isNotEmpty()) {
                Toast.makeText(this, "Szukam na: ${mySources[sourceIndex]}", Toast.LENGTH_SHORT).show()
                phantomView.loadUrl("https://${mySources[sourceIndex]}/szukaj/$movie")
            }
        }

        ui.addView(input); ui.addView(btn); ui.addView(scroll)
        root.addView(ui)
        setContentView(root)
    }

    private fun parseAndDisplay(value: String, container: LinearLayout) {
        val clean = value.replace("[", "").replace("]", "").replace("\"", "")
        val lines = clean.split(",")
        runOnUiThread {
            lines.forEach { line ->
                val parts = line.split("|||")
                if (parts.size == 2 && parts[0].length > 2) {
                    val b = Button(this).apply {
                        text = parts[0].trim()
                        setOnClickListener { 
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(parts[1]))) 
                        }
                    }
                    container.addView(b)
                }
            }
        }
    }
    companion object { init { System.loadLibrary("videoscout") } }
}
