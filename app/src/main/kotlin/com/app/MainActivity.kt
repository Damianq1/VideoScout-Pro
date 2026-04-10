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
            setHintTextColor(Color.GRAY)
        }

        val filterPanel = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL 
            setPadding(0, 10, 0, 10)
        }
        
        listOf("Lektor PL", "Napisy", "HD").forEach { filter ->
            val cb = CheckBox(this).apply {
                text = filter
                setTextColor(Color.LTGRAY)
                setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) activeFilters.add(filter) else activeFilters.remove(filter)
                }
            }
            filterPanel.addView(cb)
        }

        val btn = Button(this).apply {
            text = "URUCHOM SKANER"
            setBackgroundColor(Color.parseColor("#BB86FC"))
            setTextColor(Color.BLACK)
        }
        
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress?.visibility = View.GONE
        
        monitor = TextView(this).apply { 
            text = "Gotowy"; setTextColor(Color.parseColor("#03DAC6")); textSize = 11f 
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
                // Naprawiony błąd referencji przez jawne wskazywanie MainActivity
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    this@MainActivity.progress?.visibility = android.view.View.VISIBLE
                    this@MainActivity.monitor?.text = "Ładowanie: " + (url?.take(30) ?: "")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    this@MainActivity.progress?.visibility = android.view.View.GONE
                    this@MainActivity.monitor?.text = "Analiza kodu strony..."
                    
                    view?.evaluateJavascript(scouter.generateDiscoveryScript()) { data ->
                        val parsed = scouter.parseJson(data)
                        updateUI(parsed, resultsArea)
                    }
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    this@MainActivity.progress?.visibility = android.view.View.GONE
                    this@MainActivity.monitor?.text = "Błąd połączenia. Spróbuj ponownie."
                }
            }
        }

        btn.setOnClickListener {
            resultsArea.removeAllViews()
            val q = input.text.toString()
            if(q.isNotEmpty()) {
                progress?.visibility = View.VISIBLE
                val fStr = activeFilters.joinToString(" ")
                val query = URLEncoder.encode("$q $fStr", "UTF-8")
                engine?.loadUrl("https://html.duckduckgo.com/html/?q=" + query)
            }
        }

        root.addView(input); root.addView(filterPanel); root.addView(btn); root.addView(progress); root.addView(monitor); root.addView(scroll)
        setContentView(root)
    }

    private fun updateUI(data: List<Pair<String, Boolean>>, container: LinearLayout) {
        runOnUiThread {
            if(data.isEmpty()) monitor?.text = "Nic nie znaleziono na tej stronie."
            data.filter { it.second }.toSet().forEach { (url, _) ->
                if (!url.contains("google") && !url.contains("duckduckgo")) {
                    val domain = Uri.parse(url).host?.replace("www.", "")?.uppercase() ?: "LINK"
                    val b = Button(this).apply {
                        text = "[$domain] OTWÓRZ"
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
