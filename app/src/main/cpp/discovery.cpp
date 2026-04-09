#include <jni.h>
#include <string>
#include <vector>

// Skupiamy się TYLKO na blokowaniu landing page'y reklamowych
std::vector<std::string> hard_blacklist = {"filmo.agency", "lp478", "onclickalgo", "bet365"};

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isBlacklisted(JNIEnv* env, jobject thiz, jstring url) {
    const char *nativeUrl = env->GetStringUTFChars(url, 0);
    std::string sUrl = nativeUrl;
    
    bool blocked = false;
    for (const auto& domain : hard_blacklist) {
        if (sUrl.find(domain) != std::string::npos) {
            blocked = true;
            break;
        }
    }
    
    env->ReleaseStringUTFChars(url, nativeUrl);
    return blocked ? JNI_TRUE : JNI_FALSE;
}
}
