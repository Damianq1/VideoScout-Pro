#ifndef CONFIG_PARSER_H
#define CONFIG_PARSER_H

#include <vector>
#include <string>

// Klasa pomocnicza do zarządzania wzorcami wyszukiwania (Dorks)
class ConfigParser {
public:
    // Zwraca domyślną listę rozszerzeń wideo do skanowania
    static std::vector<std::string> getDefaultExtensions() {
        return {".mp4", ".mkv", ".avi", ".mov"};
    }

    // Przykładowa funkcja filtrująca (logika skanera)
    static bool isVideoFile(const std::string& filename) {
        for (const auto& ext : getDefaultExtensions()) {
            if (filename.size() >= ext.size() && 
                filename.compare(filename.size() - ext.size(), ext.size(), ext) == 0) {
                return true;
            }
        }
        return false;
    }
};

#endif
