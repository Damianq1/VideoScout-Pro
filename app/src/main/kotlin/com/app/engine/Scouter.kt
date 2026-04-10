package com.app.engine

import org.json.JSONArray

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                const scan = () => {
                    let found = [];
                    // 1. Skanowanie wszystkiego co ma atrybuty linkowe
                    let all = document.querySelectorAll('*');
                    all.forEach(el => {
                        let link = el.href || el.src || el.getAttribute('data-src') || el.getAttribute('data-url');
                        if (link && link.startsWith('http')) {
                            let text = (el.innerText || el.title || el.alt || '').trim();
                            found.push({url: link, title: text});
                        }
                    });

                    // 2. Skanowanie ukrytych skryptów (RAW Regex)
                    let rawHTML = document.documentElement.innerHTML;
                    let urlRegex = /(https?:\/\/[^\s"'<>]+)/g;
                    let match;
                    while ((match = urlRegex.exec(rawHTML)) !== null) {
                        found.push({url: match[1], title: 'Link z kodu'});
                    }

                    if(window.AndroidInterface) {
                        window.AndroidInterface.sendResults(JSON.stringify(found));
                    }
                };
                // Próbujemy skanować od razu i po 3 sekundach
                scan();
                setTimeout(scan, 3000);
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
                val url = obj.getString("url")
                if (!url.contains("google") && !url.contains("gstatic") && url.length > 10) {
                    list.add(url to obj.optString("title", "SPRAWDŹ"))
                }
            }
        } catch (e: Exception) {}
        return list.distinctBy { it.first }
    }
}
