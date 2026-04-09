#include <jni.h>
#include <string>

extern "C" {
// Klucz do dynamicznej listy (np. Twój prywatny Gist na GitHubie z listą domen)
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_getUpdateUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("https://raw.githubusercontent.com/user/repo/main/sources.txt");
}
}
