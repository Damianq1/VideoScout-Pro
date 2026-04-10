package com.app.engine

class Scouter {
    fun generateDiscoveryScript(): String {
        return """
            (function(){
                // Funkcja skanująca wywoływana z opóźnieniem
                setTimeout(() => {
                    let results = [];
                    let allElems = document.getElementsByTagName('*');
                    
                    for (let el of allElems) {
                        let attrs = ['src', 'href', 'data-src', 'data-url'];
                        attrs.forEach(attr => {
                            let val = el.getAttribute(attr);
                            if (val && val.startsWith('http')) {
                                // Agresywne filtrowanie słów kluczowych
                                let isVideo = /video|movie|film|watch|embed|player|serial|v=|\.mp4|\.m3u8|vidoza|vidload|upstream/.test(val.toLowerCase());
                                if(isVideo) results.push({url: val, priority: true});
                            }
                        });
                    }
                    
                    // Przekazanie wyników do Androida przez specjalny interfejs
                    window.AndroidInterface.sendResults(JSON.stringify(results));
                }, 2000); // 2 sekundy zwłoki na wczytanie playerów
                return "Scanning started...";
            })();
        """.trimIndent()
    }

    fun parseJson(json: String?): List<Pair<String, Boolean>> {
        if (json == null || json == "null" || json == "[]") return emptyList()
        val list = mutableListOf<Pair<String, Boolean>>()
        try {
            // Czyścimy formatowanie JSON wysłane z JS
            val items = json.split("},{")
            for (item in items) {
                val url = item.substringAfter("url:\"").substringBefore("\",")
                if (url.startsWith("http")) {
                    list.add(url to true)
                }
            }
        } catch (e: Exception) {}
        return list
    }
}
