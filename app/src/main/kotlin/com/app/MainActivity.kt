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
    private var onlySubscribed = true // Domyślnie szukamy w Twojej bazie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            setPadding(25, 25, 25, 25)
        }

        val input = EditText(this).apply {
            hint = "Tytuł filmu..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        // --- WYBÓR TRYBU ---
        val modeSwitch = Switch(this).apply {
            text = "TYLKO MOJA BAZA (SUBSKRYPCJE)"
            setTextColor(Color.LTGRAY)
            isChecked = true
            setOnCheckedChangeListener { _, isChecked -> 
                onlySubscribed = isChecked
                text = if(isChecked) "TYLKO MOJA BAZA" else "WYSZUKIWANIE GLOBALNE"
            }
        }

        val btn = Button(this).apply {
            text = "SZUKAJ FILMU"
            setBackgroundColor(Color.parseColor("#BB86FC"))
            setTextColor(Color.BLACK)
        }
        
        monitor = TextView(this).apply { 
            text = "Gotowy"; setTextColor(Color.GREEN); setPadding(0, 10, 0, 10) 
        }

        resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        val web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(object {
                @JavascriptInterface
                fun sendResults(data: String) {
                    runOnUiThread {
                        val results = scouter.parseJson(data)
                        displayResults(results)
                    }
                }
            }, "AndroidInterface")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    monitor?.text = "Skanowanie treści..."
                    view?.evaluateJavascript(scouter.generateDiscoveryScript(), null)
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                monitor?.text = "Łączenie ze źródłami..."
                val fullQuery = scouter.generateSearchQuery(q, onlySubscribed)
                val url = "https://www.google.com/search?q=" + URLEncoder.encode(fullQuery, "UTF-8")
                web.loadUrl(url)
            }
        }

        root.addView(input); root.addView(modeSwitch); root.addView(btn); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun displayResults(data: List<Pair<String, String>>) {
        if(data.isEmpty()) {
            monitor?.text = "Brak wyników. Zmień tryb wyszukiwania."
            return
        }
        monitor?.text = "Znaleziono: ${data.size} pozycji"
        data.forEach { (url, title) ->
            if(!url.contains("google")) {
                val b = Button(this).apply {
                    val domain = Uri.parse(url).host?.replace("www.", "")?.uppercase() ?: "LINK"
                    text = "[$domain] ${title.take(15)}"
                    setBackgroundColor(Color.parseColor("#1E1E1E"))
                    setTextColor(Color.CYAN)
                    setOnClickListener { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) }
                }
                resultsArea.addView(b)
            }
        }
    }
}
