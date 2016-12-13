//
// Created by zhengxin on 2016/12/13.
//

#ifndef MEDIACOREDEMO_COMMON_LOG_H
#define MEDIACOREDEMO_COMMON_LOG_H

#include <android/log.h>
#define LOG_INFO(TAG,format,...) __android_log_print(ANDROID_LOG_INFO, TAG,format, __VA_ARGS__)
#endif //MEDIACOREDEMO_COMMON_LOG_H
