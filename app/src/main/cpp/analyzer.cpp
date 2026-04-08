#include <string>

/**
 * Funkcja: verifyVideoDuration
 * Opis: Sprawdza czy wideo mieści się w zadanym limicie czasowym.
 * @param duration: czas w sekundach
 * @return: true jeśli wideo jest poprawne
 */
bool verifyVideoDuration(int duration) {
    // Linia 11: Minimalny czas (np. 60s dla pełnych filmów)
    const int MIN_DURATION = 60;
    // Linia 13: Logika sprawdzająca
    return (duration >= MIN_DURATION);
}

/**
 * Funkcja: getFileFormat
 * Opis: Wyciąga rozszerzenie z adresu URL.
 */
std::string getFileFormat(std::string url) {
    if (url.find(".mp4") != std::string::npos) return "MP4";
    if (url.find(".mkv") != std::string::npos) return "MKV";
    return "UNKNOWN";
}
