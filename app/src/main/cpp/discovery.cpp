#include <jni.h>
#include <string>

extern "C" {

// Funkcja zwracająca listę dostępnych plików do wyboru
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_fetchAvailableStreams(JNIEnv* env, jobject thiz, jstring query) {
    const char *nativeQuery = env->GetStringUTFChars(query, 0);
    
    // Symulacja znalezionych plików na podstawie frazy
    std::string list = "STREAM_ID_1|Wideo_720p_HD.mp4|120MB\n";
    list += "STREAM_ID_2|Wideo_1080p_FullHD.mkv|450MB\n";
    list += "STREAM_ID_3|Podgląd_Niskiej_Jakości.3gp|15MB";

    env->ReleaseStringUTFChars(query, nativeQuery);
    return env->NewStringUTF(list.c_str());
}

// Funkcja startująca pobieranie konkretnego ID
JNIEXPORT jstring JNICALL
Java_com_app_MainActivity_startSelectedDownload(JNIEnv* env, jobject thiz, jstring streamId, jstring savePath) {
    const char *id = env->GetStringUTFChars(streamId, 0);
    const char *path = env->GetStringUTFChars(savePath, 0);
    
    std::string fullPath = std::string(path) + "/" + std::string(id) + ".mp4";
    
    // Symulacja fizycznego zapisu wybranego pliku
    FILE* f = fopen(fullPath.c_str(), "wb");
    if (f) {
        fprintf(f, "DATA_FOR_%s", id);
        fclose(f);
    }

    env->ReleaseStringUTFChars(streamId, id);
    env->ReleaseStringUTFChars(savePath, path);
    return env->NewStringUTF(fullPath.c_str());
}

}
