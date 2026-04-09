#include <jni.h>
#include <string>

extern "C" {
// Silnik sprawdzający czy link nie jest "śmieciem" płatnym
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isDomainSafe(JNIEnv* env, jobject thiz, jstring domain) {
    const char *nativeDomain = env->GetStringUTFChars(domain, 0);
    std::string d = nativeDomain;
    // Agresywnie wycinamy wszystko co nie jest naszą bazą
    bool safe = (d.find("cda.pl") != std::string::npos || d.find("vider") != std::string::npos);
    env->ReleaseStringUTFChars(domain, nativeDomain);
    return safe ? JNI_TRUE : JNI_FALSE;
}
}
