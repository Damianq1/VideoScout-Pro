#include <jni.h>
#include <string>
#include <vector>

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isDomainSafe(JNIEnv* env, jobject thiz, jstring domain) {
    const char *nativeDomain = env->GetStringUTFChars(domain, 0);
    std::string d = nativeDomain;
    
    // Rozszerzona lista blokad (Punkt 3 i Twoje screeny)
    std::vector<std::string> blacklist = {
        "google", "youtube", "netflix", "disney", "hbo", 
        "apple", "amazon", "player.pl", "canalplus", "vod.pl",
        "upflix", "filmweb", "justwatch", "netia", "orange", "t-mobile"
    };
    
    bool safe = true;
    for (const auto& site : blacklist) {
        if (d.find(site) != std::string::npos) {
            safe = false;
            break;
        }
    }

    env->ReleaseStringUTFChars(domain, nativeDomain);
    return safe ? JNI_TRUE : JNI_FALSE;
}
}
