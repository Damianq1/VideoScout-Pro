#include <jni.h>
#include <string>

extern "C" {

// Funkcja testowa: Sprawdza czy biblioteka Native została poprawnie załadowana
// Zwraca prosty ciąg znaków do warstwy Kotlin/Java
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_checkNativeHealth(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Native Bridge: Połączono poprawnie");
}

}
