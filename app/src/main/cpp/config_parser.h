#ifndef CONFIG_PARSER_H
#define CONFIG_PARSER_H

#include <string>
#include <vector>
#include <fstream>
#include "logger.h"

/**
 * Funkcja: loadDorksFromSettings
 * Opis: Wczytuje dorki z pliku settings.json.
 */
std::vector<std::string> loadDorksFromSettings(std::string path) {
    std::vector<std::string> dorks;
    std::ifstream file(path);
    std::string line;
    
    // Linia 16: Proste szukanie fraz w cudzysłowie (uproszczony parser JSON)
    while (std::getline(file, line)) {
        if (line.find("\"") != std::string::npos && line.find(":") == std::string::npos) {
            size_t start = line.find("\"") + 1;
            size_t end = line.find("\"", start);
            dorks.push_back(line.substr(start, end - start));
        }
    }
    LOGI("Załadowano %d dorków z konfiguracji", (int)dorks.size());
    return dorks;
}
#endif
