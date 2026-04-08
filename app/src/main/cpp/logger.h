#ifndef LOGGER_H
#define LOGGER_H

#include <android/log.h>

// Linia 7: Definicja tagu dla Logcat
#define LOG_TAG "VideoScout2_0"

// Linia 10: Makra dla różnych poziomów logowania
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif
