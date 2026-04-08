#ifndef HTTP_CLIENT_H
#define HTTP_CLIENT_H

#include <string>
#include "logger.h"

/**
 * Funkcja: fetchUrlContent
 * Opis: Pobiera kod HTML strony w celu poszukiwania linków .mp4/.mkv
 */
std::string fetchUrlContent(std::string url) {
    // Linia 12: Logowanie próby połączenia
    LOGI("Łączenie z hostem: %s", url.c_str());

    // Linia 15: Miejsce na implementację gniazd (sockets) lub libcurl
    // Na potrzeby testów zwracamy symulowany HTML
    return "<html><body><a href='test_video.mp4'>Link</a></body></html>";
}

#endif
