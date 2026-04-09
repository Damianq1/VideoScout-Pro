package com.app.engine

import android.net.Uri

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                let results = [];
                document.querySelectorAll('a, iframe, video').forEach(el => {
                    let href = el.href || el.src;
                    if(href && href.startsWith('http')) {
                        let isVideo = /video|movie|film|watch|embed|player|serial|v=/.test(href.toLowerCase());
                        results.push({url: href, priority: isVideo});
                    }
                });
                return JSON.stringify(results);
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, Boolean>> {
        if (json == null || json == "null") return emptyList()
        return json.replace("\"", "").replace("[{url:", "").replace("}]", "")
            .split("},{url:").mapNotNull { entry ->
                val parts = entry.split(",priority:")
                if (parts.size >= 2) parts[0] to parts[1].contains("true") else null
            }
    }
}
