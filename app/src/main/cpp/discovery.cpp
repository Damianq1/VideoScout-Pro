#include <jni.h>
#include <string>
#include <unistd.h>

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_startBuffering(JNIEnv* env, jobject thiz, jstring url) {
    // Symulacja aktywnego strumieniowania w C++ (Punkt 5)
    // W przyszłości tu wejdzie libcurl do przechwytywania .ts
    return env->NewStringUTF("BUFFERING_STARTED");
}
}
