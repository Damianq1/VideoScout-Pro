#include <jni.h>
#include <random>
#include <thread>

extern "C" {
JNIEXPORT void JNICALL
Java_com_app_MainActivity_applyJitter(JNIEnv* env, jobject thiz) {
    // Losowe opóźnienie 200-800ms przed każdym żądaniem (Punkt 5)
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(200, 800);
    std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
}
}
