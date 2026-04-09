#include <jni.h>
#include <string>
#include <vector>

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isDomainSafe(JNIEnv* env, jobject thiz, jstring domain) {
    const char *nativeDomain = env->GetStringUTFChars(domain, 0);
    std::string d = nativeDomain;
    
    // Czarna lista płatnych gigantów i śmieciowych domen (Punkt 3)
    std::vector<std::string> blacklist = {
        "google", "youtube", "netflix", "disneyplus", "hbo", 
        "amazon", "primevideo", "player.pl", "canalplus", 
        "apple", "netia", "facebook", "wikipedia"
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
