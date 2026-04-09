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
    
    // Autonomiczna baza znaleziona podczas pracy
    private val discoveredSources = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A0A"))
            setPadding(30, 30, 30, 30)
        }

        val input = EditText(this).apply {
            hint = "Wpisz film (np. Mavka)..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.DKGRAY)
            setBackgroundColor(Color.parseColor("#151515"))
        }
        
        val btn = Button(this).apply {
            text = "AUTONOMICZNY SKAUTING"
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
        }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
        }
        
        monitor = TextView(this).apply {
            text = "Silnik: Gotowy"; setTextColor(Color.parseColor("#BB86FC"))
        }

        val resultsArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(resultsArea)
        }

        engine = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    this@MainActivity.progress?.visibility = View.GONE
                    
                    // Skrypt, który nie tylko szuka linków, ale i ocenia domeny
                    val discoveryJS = """
                        (function(){
                            let results = [];
                            let links = document.querySelectorAll('a, iframe');
                            links.forEach(l => {
                                let href = l.href || l.src;
                                if(href && href.startsWith('http')) {
                                    // Sprawdzanie czy link wygląda na wideo/stronę filmową
                                    let isVideo = /video|movie|film|watch|embed|player|serial/.test(href.toLowerCase());
                                    results.push({url: href, priority: isVideo});
                                }
                            });
                            return JSON.stringify(results);
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(discoveryJS) { data ->
                        processAutonomousData(data, resultsArea)
                    }
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                monitor?.text = "Analiza sieciowa w toku..."
                // Agresywne zapytanie omijające cenzurę
                val query = URLEncoder.encode("$q (site:pl | site:cc | site:to | site:info) lektor", "UTF-8")
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + query)
            }
        }

        root.addView(input); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun processAutonomousData(json: String?, container: LinearLayout) {
        if (json == null || json == "null") return
        
        // Czyszczenie prostego JSONa z JS
        val raw = json.replace("\"", "").replace("[{url:", "").replace("}]", "").split("},{url:")
        
        runOnUiThread {
            raw.forEach { entry ->
                val parts = entry.split(",priority:")
                if (parts.size >= 2) {
                    val url = parts[0].trim()
                    val isHighPriority = parts[1].contains("true")
                    val domain = Uri.parse(url).host ?: ""

                    // Jeśli domena jest nowa i wygląda na wideo, dodaj do odkryć
                    if (isHighPriority && !discoveredSources.contains(domain)) {
                        discoveredSources.add(domain)
                        monitor?.text = "Odkryto nowe źródło: $domain"
                    }

                    // Wyświetlaj tylko "mięso" - linki o wysokim priorytecie
                    if (isHighPriority && !url.contains("google") && !url.contains("duckduckgo")) {
                        val b = Button(this).apply {
                            val siteTag = domain.uppercase().replace("WWW.", "")
                            text = "[$siteTag] -> ODKRYTO TREŚĆ"
                            setBackgroundColor(Color.parseColor("#1E1E1E"))
                            setTextColor(Color.parseColor("#03DAC6"))
                            setOnClickListener { 
                                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                        container.addView(b)
                    }
                }
            }
        }
    }
}
