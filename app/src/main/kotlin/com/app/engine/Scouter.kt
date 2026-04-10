package com.app.engine

import org.json.JSONArray

class Scouter {
    // Twoja baza - "Subskrypcje"
    val subscribedSources = listOf(
        "iitv.info", "zaluknij.cc", "virpe.cc", "ekino-tv.pl", 
        "obejrzyj.to", "cda-hd.cc", "film.telewizjada.xyz", 
        "filmyonline.cc", "teletivi.pl", "filman.cc", 
        "vizjer.site", "filser.cc", "telekino.top"
    )

    fun generateSearchQuery(title: String, onlySubscribed: Boolean): String {
        return if (onlySubscribed) {
            // Generuje: "tytuł (site:strona1.pl OR site:strona2.cc ...)"
            val sites = subscribedSources.joinToString(" OR ") { "site:$it" }
            "$title ($sites)"
        } else {
            "$title film online lektor"
        }
    }

    fun generateDiscoveryScript(): String {
        return """
            (function(){
                setTimeout(() => {
                    let found = [];
                    document.querySelectorAll('a, iframe, video').forEach(el => {
                        let link = el.href || el.src || el.getAttribute('data-src');
                        if (link && link.startsWith('http')) {
                            let text = (el.innerText || el.title || 'Źródło').trim().substring(0, 25);
                            found.push({url: link, title: text});
                        }
                    });
                    if(window.AndroidInterface) window.AndroidInterface.sendResults(JSON.stringify(found));
                }, 2000);
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, String>> {
        if (json.isNullOrEmpty() || json == "null") return emptyList()
        val list = mutableListOf<Pair<String, String>>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(obj.getString("url") to obj.getString("title"))
            }
        } catch (e: Exception) {}
        return list.distinctBy { it.first }
    }
}
