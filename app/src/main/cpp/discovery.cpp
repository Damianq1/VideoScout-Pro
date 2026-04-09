#include <jni.h>
#include <string>
#include <vector>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_checkNativeHealth(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Silnik VideoScout 2.0: Aktywny");
}

// Funkcja skanująca z obsługą zapytania i dodatkowej bazy
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_startAdvancedScan(JNIEnv* env, jobject thiz, jstring query, jstring extraLink) {
    const char *nativeQuery = env->GetStringUTFChars(query, 0);
    const char *nativeLink = env->GetStringUTFChars(extraLink, 0);
    
    std::string result = "Inicjacja skanowania: " + std::string(nativeQuery) + "\n";
    
    if (std::string(nativeLink).length() > 0) {
        result += "Dodatkowa baza: " + std::string(nativeLink) + "\n";
    }

    result += "Status: Przechwytywanie strumienia (Stream Recording Mode)...\n";
    result += "[INFO]: Buforowanie i nadpisywanie na dysk aktywne.\n";

    env->ReleaseStringUTFChars(query, nativeQuery);
    env->ReleaseStringUTFChars(extraLink, nativeLink);
    return env->NewStringUTF(result.c_str());
}

}
