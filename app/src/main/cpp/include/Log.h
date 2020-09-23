//
// Created by Jianlin on 21/09/2020.
//

#pragma once

#include <android/log.h>

#define NDK_DEBUG 1

#ifdef NDK_DEBUG
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"TEST-NDK", __VA_ARGS__)
#endif

