#ifndef THREAD_POOL_H
#define THREAD_POOL_H

#include <vector>
#include <thread>
#include <future>
#include <functional>

/**
 * Funkcja: executeParallelScan
 * Opis: Uruchamia zadania skanowania w osobnych wątkach.
 */
void executeParallelScan(std::vector<std::string> urls) {
    std::vector<std::thread> workers;
    
    // Linia 15: Tworzenie wątków dla każdego adresu URL
    for (const auto& url : urls) {
        workers.push_back(std::thread([url]() {
            // Tutaj wywołamy nasz fetchUrlContent z poprzedniego kroku
            LOGI("Wątek pracuje nad: %s", url.c_str());
        }));
    }

    // Linia 22: Czekanie na zakończenie wszystkich zadań
    for (auto& t : workers) {
        if (t.joinable()) t.join();
    }
}

#endif
