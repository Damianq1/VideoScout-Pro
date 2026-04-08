#include <string>
#include <vector>
#include "logger.h"

/**
 * Funkcja: buildGoogleQuery
 * Opis: Łączy dork z zapytaniem użytkownika.
 */
std::string buildGoogleQuery(std::string userQuery, std::string dork) {
    // Linia 11: Budowanie pełnego ciągu zapytania
    std::string fullQuery = userQuery + " " + dork;
    
    // Linia 14: Logowanie operacji (widoczne w Android Studio/Logcat)
    LOGI("Budowanie zapytania: %s", fullQuery.c_str());
    
    return fullQuery;
}

/**
 * Funkcja: isVideoLink
 * Opis: Szybka weryfikacja czy link prowadzi do pliku wideo.
 */
bool isVideoLink(std::string url) {
    // Linia 24: Sprawdzanie popularnych rozszerzeń
    if (url.find(".mp4") != std::string::npos) return true;
    if (url.find(".mkv") != std::string::npos) return true;
    return false;
}
