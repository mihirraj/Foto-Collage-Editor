#include "Filter.h"

Filter::Filter(JNIEnv * env, jobject jbitmap):m_env(env), m_pixels(0), m_loc(-1), m_bitmap(jbitmap) {
	//m_loc = getBitmapInfo(m_env, m_bitmap, &m_pixels, &m_bitmapInfo);
}

Filter::~Filter() {
	if (m_loc > 0) {
	//	AndroidBitmap_unlockPixels(m_env, m_bitmap);
	}
}
/*
int Filter::getBitmapInfo(JNIEnv* env, jobject jbitmap, void** pixels, AndroidBitmapInfo* info) {
	int ret = 0;

	if ((ret = AndroidBitmap_getInfo(env, jbitmap, info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return ret;
	}
	if (info->format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGB_8888! format=%d", info->format);
		return -1;
	}
	return AndroidBitmap_lockPixels(env, jbitmap, pixels);
}
*/
int Filter::constrain(int value, int min, int max) {
    if (value < min) {
      return min;
    }
    if (value > max) {
      return max;
    }
    return value;
}

void Filter::rgb2hsv(uint8_t r, uint8_t g, uint8_t b, float & h, float & s, float & v) {
	uint8_t maxC = b;
	maxC = r > maxC ? r : maxC;
	maxC = g > maxC ? g : maxC;

	uint8_t minC = b;
	minC = r < minC ? r : minC;
	minC = g < minC ? g : minC;

	h = 0;
	s = 0;
	v = maxC;

	uint8_t delta = maxC - minC;

	if (delta == 0) {
		return;
	}

	if (maxC == r) {
		h = (g - b) / (float)delta;
		if(h < 0.0)	{
			h += 6.0f;
		}
	} else if (maxC == g) {
		h = (b - r) / (float)delta + 2.0f;
	} else {
		h = (r - g) / (float)delta + 4.0f;
	}
	h *= 60.0f;
	s = delta / (float)maxC;
}

void Filter::hsv2rgb(float h, float s, float v, uint8_t & r, uint8_t & g, uint8_t & b) {
	float chroma = s * v;
	h /= 60.0f;
	int hi = ((int)h) % 6;
	float f = h - floorf(h);
	float p = v * (1.0 - s);
	float q = v * (1.0 - (f * s));
	float t = v * (1.0 - ((1.0 - f) * s));
	switch (hi) {
	case 0: r = v; g = t; b = p;
	case 1: r = q; g = v; b = p;
	case 2: r = p; g = v; b = t;
	case 3: r = p; g = q; b = v;
	case 4: r = t; g = p; b = v;
	case 5: r = v; g = p; b = q;
	}
}


