#ifndef _FILTER_H_
#define _FILTER_H_

#include <jni.h>
//#include <android/bitmap.h>
#include <android/log.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} rgba;

class Filter {

public:
	Filter(JNIEnv * env, jobject jbitmap);
	virtual ~Filter();
	virtual void process() = 0;

	inline static uint8_t alpha(uint32_t color) { return (color >> 24) & 0xFF; }
	inline static uint8_t red(uint32_t color) { return (color >> 16) & 0xFF; }
	inline static uint8_t green(uint32_t color) { return (color >> 8) & 0xFF; }
	inline static uint8_t blue(uint32_t color) { return color & 0xFF; }
	inline static uint32_t rgb(uint8_t r, uint8_t g, uint8_t b) {return (0xFF << 24) | (r << 16) | (g << 8) | b;}

	static void rgb2hsv(uint8_t r, uint8_t g, uint8_t b, float & h, float & s, float & v);
	static void hsv2rgb(float h, float s, float v, uint8_t & r, uint8_t & g, uint8_t & b);

	//static int getBitmapInfo(JNIEnv* env, jobject jbitmap, void** pixels, AndroidBitmapInfo* info);
	static int constrain(int value, int min, int max);

protected:
	//AndroidBitmapInfo m_bitmapInfo;
	void * m_pixels;
	JNIEnv * m_env;
	int m_loc;
	jobject m_bitmap;
};

#endif

