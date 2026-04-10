package com.app.engine

import android.net.Uri

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                let results = [];
                // 1. Wyciągnij wszystko co wygląda jak URL z całego kodu HTML i atrybutów
                let allElems = document.getElementsByTagName('*');
                let linkRegex = /(https?:\/\/[^\s"'<>]+)/g;
                
                // Szukaj w atrybutach (src, href, data-src, value)
                for (let el of allElems) {
                    let attrs = ['src', 'href', 'data-src', 'data-url', 'value'];
                    attrs.forEach(attr => {
                        let val = el.getAttribute(attr);
                        if (val && val.startsWith('http')) {
                            let isVideo = /video|movie|film|watch|embed|player|serial|v=|\.mp4|\.m3u8/.test(val.toLowerCase());
                            results.push({url: val, priority: isVideo});
                        }
                    });
                }

                // 2. Szukaj wewnątrz bloków <script> (częste dla playerów JS)
                let scripts = document.getElementsByTagName('script');
                for (let s of scripts) {
                    let match;
                    while ((match = linkRegex.exec(s.innerHTML)) !== null) {
                        let url = match[1];
                        if (url.includes('cdn') || url.includes('player') || url.includes('m3u8')) {
                            results.push({url: url, priority: true});
                        }
                    }
                }
                return JSON.stringify(results);
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, Boolean>> {
        if (json == null || json == "null" || json == "[]") return emptyList()
        // Ulepszone parsowanie dla niestandardowych znaków w URL
        val list = mutableListOf<Pair<String, Boolean>>()
        try {
            val clean = json.removePrefix("[").removeSuffix("]")
            val items = clean.split("},{")
            for (item in items) {
                val url = item.substringAfter("url:").substringBefore(",priority:")
                val priority = item.substringAfter("priority:").substringBefore("}").contains("true")
                if (url.startsWith("http")) {
                    list.add(url to priority)
                }
            }
        } catch (e: Exception) {}
        return list
    }
}
