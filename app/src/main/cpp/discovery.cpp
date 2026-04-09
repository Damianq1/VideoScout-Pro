#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isDomainSafe(JNIEnv* env, jobject thiz, jstring domain) {
    const char *nativeDomain = env->GetStringUTFChars(domain, 0);
    std::string d = nativeDomain;
    // Blokujemy gigantów, szukamy niszowych stron wideo
    bool unsafe = (d.find("google") != std::string::npos || d.find("youtube") != std::string::npos);
    env->ReleaseStringUTFChars(domain, nativeDomain);
    return unsafe ? JNI_FALSE : JNI_TRUE;
}
}
