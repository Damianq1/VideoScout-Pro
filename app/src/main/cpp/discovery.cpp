#include <jni.h>

static int depth_counter = 0;

extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_app_MainActivity_canContinue(JNIEnv* env, jobject thiz) {
    if (depth_counter > 10) {
        depth_counter = 0;
        return JNI_FALSE;
    }
    depth_counter++;
    return JNI_TRUE;
}
}
