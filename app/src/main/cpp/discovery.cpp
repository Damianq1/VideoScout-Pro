#include <jni.h>
#include <string>
#include <vector>
#include "logger.h"
#include "http_client.h"
#include "thread_pool.h"
#include "config_parser.h"
#include "filter.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_runVideoScout(JNIEnv* env, jobject thiz, jstring query) {
    const char *nativeQuery = env->GetStringUTFChars(query, 0);
    
    // 1. Ładowanie dorków
    std::vector<std::string> dorks = loadDorksFromSettings("/data/data/com.app/files/settings.json");
    
    // 2. Budowanie celów i skanowanie równoległe
    std::vector<std::string> targets;
    for(auto& d : dorks) targets.push_back(std::string(nativeQuery) + " " + d);
    
    executeParallelScan(targets); // Uruchamia wątki z modułu thread_pool.h

    std::string finalMsg = "Skanowanie zakończone. Sprawdź listę wyników.";
    
    env->ReleaseStringUTFChars(query, nativeQuery);
    return env->NewStringUTF(finalMsg.c_str());
}
