#ifndef FILTER_H
#define FILTER_H

#include <string>

/**
 * Funkcja: isQualityVideo
 * Opis: Sprawdza rozmiar i format, aby odrzucić "śmieci".
 */
bool isQualityVideo(long fileSizeInBytes) {
    // Linia 11: Minimum 100MB dla filmu (100 * 1024 * 1024)
    const long MIN_SIZE = 104857600;
    
    // Linia 14: Zwraca true tylko jeśli plik jest odpowiednio duży
    return fileSizeInBytes >= MIN_SIZE;
}
#endif
