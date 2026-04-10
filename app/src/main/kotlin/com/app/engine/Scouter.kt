package com.app.engine

import org.json.JSONArray

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                setTimeout(() => {
                    let found = [];
                    // Szukamy elementów, które mogą być przyciskami lub linkami do filmów
                    document.querySelectorAll('a, button, div, span, iframe').forEach(el => {
                        let text = el.innerText ? el.innerText.toLowerCase() : '';
                        let link = el.href || el.getAttribute('data-src') || el.src;
                        
                        if (link && link.startsWith('http')) {
                            // "Bystre" słowa kluczowe - szukamy kontekstu wokół linku
                            let context = text + ' ' + el.className + ' ' + el.id;
                            let isMovieContext = /oglądaj|watch|lektor|napisy|dubbing|720p|1080p|serwer|host|player|vidoza|upstream|voe|dood/.test(context.toLowerCase());
                            
                            // Jeśli link prowadzi do znanych playerów lub ma filmowy kontekst - bierzemy go
                            if (isMovieContext || /v=|embed|\.mp4|\.m3u8/.test(link.toLowerCase())) {
                                found.push({
                                    url: link, 
                                    title: text.substring(0, 30).trim() || 'Link Bezpośredni'
                                });
                            }
                        }
                    });
                    
                    if(window.AndroidInterface) {
                        window.AndroidInterface.sendResults(JSON.stringify(found));
                    }
                }, 2500);
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, String>> {
        if (json.isNullOrEmpty() || json == "null" || json == "[]") return emptyList()
        val list = mutableListOf<Pair<String, String>>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(obj.getString("url") to obj.getString("title"))
            }
        } catch (e: Exception) { }
        return list
    }
}
