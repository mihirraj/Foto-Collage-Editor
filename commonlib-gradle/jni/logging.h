#ifndef _LOGGING_H_
#define _LOGGING_H_

// platform dependent logger, should be replaced with something different for iOS

#define  LOG_TAG    "libprocessing"

#ifdef __ANDROID__  // ANDROID SPECIFIC LOGGING

#include <android/log.h>

#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#else  // NOT ANDROID

#define  LOGI(...)  std::cout << "INFO: " << LOG_TAG << ": "; printf (__VA_ARGS__); std::cout << std::endl;
#define  LOGE(...)  std::cout << "ERROR: "<< LOG_TAG << ": "; printf (__VA_ARGS__); std::cout << std::endl;

#endif

#endif