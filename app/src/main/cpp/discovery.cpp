#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jint JNICALL
Java_com_app_MainActivity_rateDomain(JNIEnv* env, jobject thiz, jstring domain) {
    const char *d = env->GetStringUTFChars(domain, 0);
    std::string s(d);
    int score = 0;
    
    // Agregatory często używają tych końcówek
    if (s.find(".cc") != std::string::npos) score += 50;
    if (s.find(".info") != std::string::npos) score += 40;
    if (s.find(".site") != std::string::npos) score += 40;
    if (s.find("film") != std::string::npos) score += 30;
    
    env->ReleaseStringUTFChars(domain, d);
    return score;
}
}
