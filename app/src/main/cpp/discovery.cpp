#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>

std::vector<std::string> blacklist = {"filman.cc", "filmo.agency", "lp478", "netflix.com"};

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_isBlacklisted(JNIEnv* env, jobject thiz, jstring url) {
    const char *nativeUrl = env->GetStringUTFChars(url, 0);
    std::string sUrl = nativeUrl;
    
    bool blocked = false;
    for (const auto& domain : blacklist) {
        if (sUrl.find(domain) != std::string::npos) {
            blocked = true;
            break;
        }
    }
    
    env->ReleaseStringUTFChars(url, nativeUrl);
    return blocked ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_app_MainActivity_addToBlacklist(JNIEnv* env, jobject thiz, jstring domain) {
    const char *nativeDomain = env->GetStringUTFChars(domain, 0);
    blacklist.push_back(std::string(nativeDomain));
    env->ReleaseStringUTFChars(domain, nativeDomain);
}
}
