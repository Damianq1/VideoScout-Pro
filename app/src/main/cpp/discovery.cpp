#include <jni.h>
#include <string>
#include "config_parser.h"

extern "C" {

// Sprawdza stan połączenia
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_checkNativeHealth(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Native Bridge: Połączono poprawnie");
}

// Symuluje start skanowania i zwraca liczbę obsługiwanych formatów
JNIEXPORT jint JNICALL
Java_com_app_MainActivity_getFormatCount(JNIEnv* env, jobject thiz) {
    return static_cast<jint>(ConfigParser::getDefaultExtensions().size());
}

}
