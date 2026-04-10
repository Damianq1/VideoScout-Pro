package com.app.engine

import org.json.JSONArray
import org.json.JSONObject

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                setTimeout(() => {
                    let found = [];
                    // Skanowanie linków i ramek
                    document.querySelectorAll('a, iframe, video, source').forEach(el => {
                        let link = el.href || el.src || el.getAttribute('data-src');
                        if (link && link.startsWith('http')) {
                            found.push({url: link});
                        }
                    });
                    
                    // Skanowanie tekstu w poszukiwaniu ukrytych URLi
                    let bodyText = document.body.innerHTML;
                    let urlRegex = /(https?:\/\/[^\s"'<>]+)/g;
                    let match;
                    while ((match = urlRegex.exec(bodyText)) !== null) {
                        found.push({url: match[1]});
                    }
                    
                    if(window.AndroidInterface) {
                        window.AndroidInterface.sendResults(JSON.stringify(found));
                    }
                }, 2500);
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, Boolean>> {
        if (json.isNullOrEmpty() || json == "null" || json == "[]") return emptyList()
        val list = mutableListOf<Pair<String, Boolean>>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val url = obj.getString("url")
                if (url.startsWith("http")) {
                    // Kryteria ważności linku
                    val isVideo = url.contains("v=") || url.contains("embed") || 
                                 url.contains(".mp4") || url.contains("player") ||
                                 url.contains("movie") || url.contains("serial")
                    list.add(url to isVideo)
                }
            }
        } catch (e: Exception) { }
        return list
    }
}
