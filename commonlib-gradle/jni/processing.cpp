#include <jni.h>
#include <vector>
#include <android/log.h>
#include <ctime>
#include <math.h>
//#include <android/bitmap.h>

#include "CurveFilter.h"
#include "BlendFilter.h"
#include "FilterException.h"
#include "FishEyeFilter.h"
#include "ColorizeHsvFilter.h"
#include "SheetDetectionFilter.h"
#include "GlitchFilter.h"
#include "HsvFilter.h"
#include "SimpleGlitchFilter.h"
//#define JNIEXPORT
//#define JNICALL

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "ImageProcessing.h"
#include "config.h"
#include "utils.h"

#define  LOG_TAG    "libprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	LOGI("JNI_OnLoad");
	return JNI_VERSION_1_4;
}

#define BUFFER_SIZE 1024*1024*2

static jbyte resultBuffer[BUFFER_SIZE];

JNIEXPORT bool Java_com_onemanwithcamera_instafish_CameraPreviewActivity_processCurrencyImage(
		JNIEnv* env, jobject obj, jstring origFileName, jstring procFileName,
		bool mirror, int angle, jbyteArray jpeg, jint width, jint height,
		jint maxWidth, jint maxHeight, bool previewEnabled, jint previewWidth, jint previewHeight, jstring previewFileName) {
	unsigned char* cjpeg;
	cjpeg = (unsigned char*) env->GetByteArrayElements(jpeg, NULL);
	cv::Mat jpegMat(height, width, CV_8UC4, cjpeg);
	cv::Mat raw = imdecode(jpegMat, -1);
	LOGI(
			"Decoded: %i x %i depth: %d channels: %d", raw.rows, raw.cols, raw.depth(), raw.channels());
	env->ReleaseByteArrayElements(jpeg, (jbyte*) cjpeg, JNI_ABORT);
	std::vector < uchar > buff; //buffer for coding
	std::vector<int> param = std::vector<int>(2);
	param[0] = CV_IMWRITE_JPEG_QUALITY;
	param[1] = 100; //default(95) 0-100
	// Get filename
	const char* outFileStr = env->GetStringUTFChars(origFileName, 0);
	const char* procFileStr = env->GetStringUTFChars(procFileName, 0);
	const char* previewFileStr = env->GetStringUTFChars(previewFileName, 0);
	// Save square image
	int side;
	if (raw.rows > raw.cols) {
		side = raw.cols;
	} else {
		if (raw.cols > raw.rows) {
			side = raw.rows;
		}
	}
	cv::Mat square;
	cv::Rect cropRect(raw.cols / 2 - side / 2, raw.rows / 2 - side / 2, side,
			side);

	cv::Mat(raw, cropRect).copyTo(square);
	LOGI("Cropped: w:%i x h:%i", square.cols, square.rows);
//    raw.release;
	// rotate image
	LOGI("Angle: %d ", angle);
	cv::Mat rotSquare;
	cv::Size rotSquareSz(side, side);
	// center
	cv::Point2f center(side / 2., side / 2.);
	cv::Mat rotMat = cv::getRotationMatrix2D(center, -angle, 1.0);
	warpAffine(square, rotSquare, rotMat, rotSquareSz);
	// mirror image if it's face camera
	if (mirror) {
		cv::Mat mirrored;
		cv::flip(rotSquare, mirrored, -1);
		rotSquare.release();
		rotSquare = mirrored;
	}
	//LOGI("filename: %s", outFileStr);
	bool res = cv::imwrite(outFileStr, rotSquare, param);
	if (previewEnabled) {
		cv::Mat preview;
		resize(rotSquare, preview, cv::Size(previewWidth * 2, previewHeight * 2), 0, 0, cv::INTER_LINEAR);
		LOGI("Preview: %i x %i depth: %d channels: %d", preview.rows, preview.cols, preview.depth(), preview.channels());
		bool res = cv::imwrite(previewFileStr, preview, param);
		raw = preview;
	} else {
		raw = rotSquare;
	}
	square.release();
	rotMat.release();
	FishEyeFilter::circleFilter(raw);
	if (raw.rows > previewHeight && raw.cols > previewWidth) {
		cv::Mat scaled;
		resize(raw, scaled, cv::Size(previewWidth, previewHeight), 0, 0,
				cv::INTER_LINEAR);
		LOGI(
				"Scaled: %i x %i depth: %d channels: %d", scaled.rows, scaled.cols, scaled.depth(), scaled.channels());
		/*bool res = imencode(".jpg", scaled, buff, param);*/
		/*LOGI("Encoded scaled result: %d, size: %d", res, buff.size());*/
		bool res = cv::imwrite(procFileStr, scaled, param);
		scaled.release();
	} else {
		/*		bool res = imencode(".jpg", raw, buff, param);*/
		/*LOGI("Encoded result: %d, size: %d", res, buff.size());*/
		bool res = cv::imwrite(procFileStr, raw, param);
	}

	/*jbyteArray result = env->NewByteArray(buff.size());
	 //jbyte resultBuffer[BUFFER_SIZE];
	 //fillTheBuffer(resultBuffer);
	 memcpy(&resultBuffer[0], &buff[0], buff.size());
	 env->SetByteArrayRegion(result, 0, buff.size(), resultBuffer);*/
	jpegMat.release();
	rotSquare.release();
	raw.release();
	env->ReleaseStringUTFChars(origFileName, outFileStr);
	env->ReleaseStringUTFChars(procFileName, procFileStr);
	env->ReleaseStringUTFChars(previewFileName, previewFileStr);
	return true;
}

JNIEXPORT void Java_com_wisesharksoftware_core_ImageProcessing_processBarrelOpenCV(
		JNIEnv* env, jobject obj, jstring inFileName, jstring outFileName,
		bool convex, int filterW, int filterH, int maxWidth, int maxHeight) {
	LOGI("processBarrel");
	std::vector<int> param = std::vector<int>(2);
	param[0] = CV_IMWRITE_JPEG_QUALITY;
	param[1] = 100; //default(95) 0-100
	const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
	const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

	cv::Mat original = cv::imread(inFileStr);

	int side = 0;
	if (original.rows > original.cols) {
		side = original.cols;
	} else {
		if (original.cols > original.rows) {
			side = original.rows;
		}
	}
	cv::Mat filtered;
	if (side != 0) {
		cv::Mat square;
		cv::Rect cropRect(original.cols / 2 - side / 2, original.rows / 2 - side / 2, side,
				side);
		cv::Mat(original, cropRect).copyTo(square);
		resize(square, filtered, cv::Size(filterW, filterH), 0, 0,
				cv::INTER_LINEAR);
		square.release();
	} else {
		resize(original, filtered, cv::Size(filterW, filterH), 0, 0, cv::INTER_LINEAR);
	}
	original.release();

	convex ?
			FishEyeFilter::barrelFilterConvex(filtered) :
			FishEyeFilter::barrelFilter(filtered);
	cv::Mat scaled;
	resize(filtered, scaled, cv::Size(maxWidth, maxHeight), 0, 0, cv::INTER_LINEAR);
	bool res = cv::imwrite(outFileStr, scaled, param);

	scaled.release();
	filtered.release();
	env->ReleaseStringUTFChars(inFileName, inFileStr);
	env->ReleaseStringUTFChars(outFileName, outFileStr);
}

std::vector<int> getJpegParams(int quality) {
  std::vector<int> param = std::vector<int>(2);
  param[0] = CV_IMWRITE_JPEG_QUALITY;
  param[1] = quality; //default(95) 0-100
  return param;
}

JNIEXPORT void Java_com_wisesharksoftware_core_ImageProcessing_processPictureOpenCV(
		JNIEnv* env, jobject obj, jstring inFileName, jstring outFileName,
		jstring origFileName, int filterW, int filterH, int maxWidth,
		int maxHeight) {
	const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
	const char* outFileStr = env->GetStringUTFChars(outFileName, 0);
	const char* origFileStr = origFileName != NULL ? env->GetStringUTFChars(origFileName, 0) : NULL;
	cv::Mat raw = cv::imread(inFileStr);

	std::vector<int> param = std::vector<int>(2);
	param[0] = CV_IMWRITE_JPEG_QUALITY;
	param[1] = 100; //default(95) 0-100
	// cropping

	// Save square image
	int side = 0;
	if (raw.rows > raw.cols) {
		side = raw.cols;
	} else {
		if (raw.cols > raw.rows) {
			side = raw.rows;
		}
	}

	cv::Mat filtered;

	if (side != 0) {
		cv::Mat square;
		cv::Rect cropRect(raw.cols / 2 - side / 2, raw.rows / 2 - side / 2, side,
				side);
		cv::Mat(raw, cropRect).copyTo(square);
		resize(square, filtered, cv::Size(filterW, filterH), 0, 0,
				cv::INTER_LINEAR);
		square.release();
	} else {
		resize(raw, filtered, cv::Size(filterW, filterH), 0, 0, cv::INTER_LINEAR);
	}
	if (origFileStr != NULL) {
		bool res = cv::imwrite(origFileStr, filtered, param);
	}
	raw = filtered;

	FishEyeFilter::circleFilter(raw);
	if (raw.rows > maxHeight && raw.cols > maxWidth) {
		cv::Mat scaled;
		resize(raw, scaled, cv::Size(maxWidth, maxHeight), 0, 0,
				cv::INTER_LINEAR);
		LOGI("Scaled: %i x %i depth: %d channels: %d", scaled.rows, scaled.cols, scaled.depth(), scaled.channels());
		/*bool res = imencode(".jpg", scaled, buff, param);*/
		/*LOGI("Encoded scaled result: %d, size: %d", res, buff.size());*/
		bool res = cv::imwrite(outFileStr, scaled, param);
		scaled.release();
	} else {
		/*		bool res = imencode(".jpg", raw, buff, param);*/
		/*LOGI("Encoded result: %d, size: %d", res, buff.size());*/
		bool res = cv::imwrite(outFileStr, raw, param);
	}

	raw.release();
	filtered.release();
	env->ReleaseStringUTFChars(inFileName, inFileStr);
	env->ReleaseStringUTFChars(outFileName, outFileStr);
	if (origFileStr != NULL) {
		env->ReleaseStringUTFChars(origFileName, origFileStr);
	}
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_BlendFilter_blendFilterOpenCV( JNIEnv* env,
                                                                                      jobject obj,
                                                                                      jstring inFileName,
                                                                                      jstring outFileName,
                                                                                      jbyteArray blendJpeg,
                                                                                      jint algorithm,
                                                                                      int blendWidth,
                                                                                      int blendHeight) {
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);

    unsigned char* blendJpegData;
    blendJpegData = (unsigned char*)env->GetByteArrayElements(blendJpeg, NULL);
    cv::Mat blendBuf(blendHeight, blendWidth, CV_8UC4, blendJpegData);
    cv::Mat blendMap = imdecode(blendBuf,
                                algorithm == ALGORITHM_TRANSPARENCY ? CV_LOAD_IMAGE_UNCHANGED : CV_LOAD_IMAGE_COLOR);
    LOGI("Decoded: %i x %i depth: %d channels: %d", blendMap.rows, blendMap.cols, blendMap.depth(), blendMap.channels() );
    env->ReleaseByteArrayElements(blendJpeg, (jbyte*)blendJpegData, JNI_ABORT);

    cv::Mat result = srcMat.clone();

    if (blendMap.cols > srcMat.cols && blendMap.rows > srcMat.rows) {
    	cv::Mat scaledMap;
    	resize(blendMap, scaledMap, cv::Size(srcMat.cols, srcMat.rows), 0, 0, cv::INTER_LINEAR);
        BlendFilter::blendFilterOpenCV(srcMat, scaledMap, result, algorithm);
        scaledMap.release();
    } else {
    	BlendFilter::blendFilterOpenCV(srcMat, blendMap, result, algorithm);
    }


    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    blendBuf.release();
    srcMat.release();
    blendMap.release();
    result.release();

    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_CurveFilter_curveFilterOpenCV( JNIEnv* env,
                                                                                      jobject obj,
                                                                                      jstring inFileName,
                                                                                      jstring outFileName,
                                                                                      jintArray redCurve,
                                                                                      jintArray greenCurve,
                                                                                      jintArray blueCurve) {
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);
    cv::Mat result = srcMat.clone();

    int* redCurveNative = env->GetIntArrayElements(redCurve, NULL);
    int* greenCurveNative = env->GetIntArrayElements(greenCurve, NULL);
    int* blueCurveNative = env->GetIntArrayElements(blueCurve, NULL);
    CurveFilter curveFilter;
    curveFilter.curveFilterOpenCV(srcMat, result, redCurveNative, greenCurveNative, blueCurveNative);

    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    srcMat.release();
    result.release();

    env->ReleaseIntArrayElements(redCurve, redCurveNative, JNI_ABORT);
    env->ReleaseIntArrayElements(greenCurve, greenCurveNative, JNI_ABORT);
    env->ReleaseIntArrayElements(blueCurve, blueCurveNative, JNI_ABORT);
    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_ColorizeHsvFilter_colorizeHsvFilterOpenCV( JNIEnv* env,
                                                                                                  jobject obj,
                                                                                                  jstring inFileName,
                                                                                                  jstring outFileName,
                                                                                                  jint hue,
                                                                                                  jint saturation,
                                                                                                  jint value) {
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);
    cv::Mat result = srcMat.clone();

    ColorizeHsvFilter::colorizeHsvFilterOpenCV(srcMat, result, hue, saturation, value);

    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    srcMat.release();
    result.release();

    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_HsvFilter_hsvFilterOpenCV( JNIEnv* env,
                                                                                                  jobject obj,
                                                                                                  jstring inFileName,
                                                                                                  jstring outFileName,
                                                                                                  jint hue,
                                                                                                  jint saturation,
                                                                                                  jint value) {
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);
    cv::Mat result = srcMat.clone();

    HsvFilter::hsvFilterOpenCV(srcMat, result, hue, saturation, value);

    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    srcMat.release();
    result.release();

    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_GlitchFilter_glitchFilterOpenCV( JNIEnv* env,
                                                                                                  jobject obj,
                                                                                                  jstring inFileName,
                                                                                                  jstring outFileName,
                                                                                                  jint hue,
                                                                                                  jint saturation,
                                                                                                  jint value) {
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);
    cv::Mat result = srcMat.clone();

    GlitchFilter::glitchFilterOpenCV(srcMat, result, hue, saturation, value);

    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    srcMat.release();
    result.release();

    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT bool Java_com_wisesharksoftware_core_filters_SimpleGlitchFilter_simpleGlitchFilterOpenCV( JNIEnv* env,
                                                                                                  jobject obj,
                                                                                                  jstring inFileName,
                                                                                                  jstring outFileName,
                                                                                                  jint dispersion,
                                                                                                  jint value) {
    if(true) return true;
    const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
    const char* outFileStr = env->GetStringUTFChars(outFileName, 0);

    cv::Mat srcMat = cv::imread(inFileStr);
    cv::Mat result = srcMat.clone();

    SimpleGlitchFilter::simpleGlitchFilterOpenCV(srcMat, result, dispersion, value);

    bool res = cv::imwrite(outFileStr, result, getJpegParams(100));

    srcMat.release();
    result.release();

    env->ReleaseStringUTFChars(inFileName, inFileStr);
    env->ReleaseStringUTFChars(outFileName, outFileStr);
    return true;
}

JNIEXPORT void Java_com_wisesharksoftware_camera_BaseCameraPreviewActivity_rotatePhotoOpenCV(
        JNIEnv* env,
        jobject obj,
        jstring fileName,
        bool mirror,
        int angle,
        bool square,
        float ratio)
{
	const char* fileNameStr = env->GetStringUTFChars(fileName, 0);
	cv::Mat raw = cv::imread(fileNameStr);

	int side = std::min(raw.cols, raw.rows);
	bool needRotate = std::abs(angle) == 90 || std::abs(angle) == 270;
	if (ratio > 0) {
		if (raw.cols > raw.rows) {
			needRotate = true;
		} else {
			needRotate = false;
		}
	}
	if (needRotate) {
		//cv::Point2f center(side / 2.0, side / 2.0);
		//warpAffine(raw, raw, cv::getRotationMatrix2D(center, -angle, 1.0), cv::Size(raw.rows, raw.cols));
		cv::transpose(raw, raw);
		cv::flip(raw, raw, 1);
	}
	if (square && (ratio < 0)) {
		cv::Rect cropRect(raw.cols / 2 - side / 2, raw.rows / 2 - side / 2, side, side);
		cv::Mat(raw, cropRect).copyTo(raw);
	}
	if (square && (ratio > 0)) {
		if (raw.cols > raw.rows) {
			cv::Rect cropRect(0, (int) ((raw.cols - raw.rows * ratio) / 2), raw.rows, (int) raw.rows * ratio);
			cv::Mat(raw, cropRect).copyTo(raw);
		} else {
			cv::Rect cropRect(0, (int) ((raw.rows - raw.cols * ratio) / 2), raw.cols, (int) raw.cols * ratio);
			cv::Mat(raw, cropRect).copyTo(raw);
		}
	}

	if (mirror) {
		cv::flip(raw, raw, -1);
	}
	bool res = cv::imwrite(fileNameStr, raw, getJpegParams(100));
	raw.release();
	env->ReleaseStringUTFChars(fileName, fileNameStr);
}
  
JNIEXPORT void Java_com_wisesharksoftware_camera_BaseCameraPreviewActivity_savePhotoOpenCV(
        JNIEnv* env,
        jobject obj,
        jstring fileName,
        bool mirror,
        int angle,
        jbyteArray jpegData,
        int width,
        int height,
        bool square,
        float ratio)
{
	unsigned char * cjpeg = (unsigned char*) env->GetByteArrayElements(jpegData, NULL);
	cv::Mat jpegMat(height, width, CV_8UC3, cjpeg);
	cv::Mat raw = cv::imdecode(jpegMat, -1);
	jpegMat.release();
	env->ReleaseByteArrayElements(jpegData, (jbyte*) cjpeg, JNI_ABORT);

	int side = std::min(raw.cols, raw.rows);
	bool needRotate = std::abs(angle) == 90 || std::abs(angle) == 270;
	if (ratio > 0) {
		if (raw.cols > raw.rows) {
			needRotate = true;
		} else {
			needRotate = false;
		}
	}
	if (needRotate) {
		//cv::Point2f center(side / 2.0, side / 2.0);
		//warpAffine(raw, raw, cv::getRotationMatrix2D(center, -angle, 1.0), cv::Size(raw.rows, raw.cols));
		cv::transpose(raw, raw);
		cv::flip(raw, raw, 1);
	}
	if (square && (ratio < 0)) {
		cv::Rect cropRect(raw.cols / 2 - side / 2, raw.rows / 2 - side / 2, side, side);
		cv::Mat(raw, cropRect).copyTo(raw);
	}
	if (square && (ratio > 0)) {
		if (raw.cols > raw.rows) {
			cv::Rect cropRect(0, (int) ((raw.cols - raw.rows * ratio) / 2), raw.rows, (int) raw.rows * ratio);
			cv::Mat(raw, cropRect).copyTo(raw);
		} else {
			cv::Rect cropRect(0, (int) ((raw.rows - raw.cols * ratio) / 2), raw.cols, (int) raw.cols * ratio);
			cv::Mat(raw, cropRect).copyTo(raw);
		}
	}

	if (mirror) {
		cv::flip(raw, raw, -1);
	}
	const char* fileNameStr = env->GetStringUTFChars(fileName, 0);
	bool res = cv::imwrite(fileNameStr, raw, getJpegParams(100));
	raw.release();
	env->ReleaseStringUTFChars(fileName, fileNameStr);
}

static ImageProcessing * gCurrentProcessing = 0;

JNIEXPORT void Java_com_wisesharksoftware_core_ImageProcessing_cancelProcessing(
          JNIEnv* env,
          jobject obj)
{
	if (gCurrentProcessing) {
		LOGI("--- fullProcessOpenCV cancel %p", gCurrentProcessing);
		gCurrentProcessing->cancelProcessing();
	} else {
		LOGI("--- fullProcessOpenCV cancel current is null");
	}
}


JNIEXPORT void Java_com_wisesharksoftware_core_ImageProcessing_fullProcessOpenCV(
          JNIEnv* env,
          jobject obj,
          jobjectArray inFiles,
          jstring outFileName,
          jstring baseAssetPath,
          // presets to apply from preset.json
          jobjectArray presets,
          jstring presetsJson,
          int maxWidth,
          int maxHeight,
          bool cancellable) {

  std::clock_t begin = std::clock();
  LOGI("--- fullProcessOpenCV cancellable=%d", cancellable);
  
  const char* outFileStr = env->GetStringUTFChars(outFileName, 0);
  const char* baseAssetPathStr = env->GetStringUTFChars(baseAssetPath, 0);
  
  //LOGI("--- outFileStr %s", outFileStr);
  //LOGI("--- baseAssetPathStr %s", baseAssetPathStr);
try{
  // init SD card path.
  Config::resourcePath = baseAssetPathStr;

  // load json presets file
  // read source file
  int imagesCount = env->GetArrayLength(inFiles);
  if (imagesCount > 1) {
	  jsonxx::Object config;
	  readConfig("preset.json", config);
	  ImageProcessing processing(config);
	  //LOGI("--- process multi pictures");
	  std::vector<cv::Mat*> images;
	  for (int i = 0; i < imagesCount; ++i) {
		  jstring inFileName = (jstring) env->GetObjectArrayElement(inFiles, i);
		  const char *inFileNameStr = env->GetStringUTFChars(inFileName, 0);
		  //LOGI("--- read file '%s'", inFileNameStr);
		  cv::Mat * image = new cv::Mat(cv::imread(inFileNameStr));
		  double ratio = (double)image->cols / (double)image->rows;
		  if (ratio >  0.5) { //To prevent resizing of cropped images for supersampler
			  resizeImage(*image, maxWidth, maxHeight);
		  }
//		  if (image->cols > maxWidth && image->rows > maxHeight) {
//			  resize(*image, *image, cv::Size(maxWidth, maxHeight), 0, 0, cv::INTER_LINEAR);
//			  //LOGI("--- image resized %d %d %d", image->cols, image->rows, image->type());
//		  }
		  //LOGI("--- image %d %d %d", image->cols, image->rows, image->type());
		  images.push_back(image);
	  }
	  int stringCount = env->GetArrayLength(presets);
	  std::vector<std::string> presetsVector;
	  for (int i=0; i<stringCount; i++) {
	    jstring presetName = (jstring) env->GetObjectArrayElement(presets, i);
	    const char *presetNameStr = env->GetStringUTFChars(presetName, 0);
		//LOGI("--- read preset '%s'", presetNameStr);
		std::string presetNameStdString(presetNameStr);
	    presetsVector.push_back(presetNameStdString);
	    env->ReleaseStringUTFChars(presetName, presetNameStr);
	  }
	  for(std::vector<cv::string>::iterator it = presetsVector.begin(); it != presetsVector.end(); ++it) {
			//LOGI("--- preset from vector '%s'", (*it).c_str());
	  }
	  cv::Mat image(cv::Size(images[0]->cols, images[0]->rows), images[0]->type());
	  //LOGI("--- create result image %d %d %d", image.cols, image.rows, image.type());
	  processing.process(images, presetsVector, image);

	  //delete all images
	  for(std::vector<cv::Mat*>::iterator it = images.begin(); it != images.end(); ++it) {
		  delete *it;
	  }
	  //LOGI("--- done!!");

	  // save thre result file
	  bool res = cv::imwrite(outFileStr, image, getJpegParams(100));

	  //LOGI("--- write %d", res);
	  // free matrix memory
	  image.release();

  } else {
	  const char *presetsJsonStr = env->GetStringUTFChars(presetsJson, 0);
	  std::string presetsJsonStdString(presetsJsonStr);
	  jsonxx::Object config;
	  if (presetsJsonStdString.empty()) {
		  readConfig("preset.json", config);
	  } else {
		  config.parse(presetsJsonStdString);
	  }
	  ImageProcessing * processing = new ImageProcessing(config);
	  if (cancellable) {
			if (gCurrentProcessing) {
				gCurrentProcessing->cancelProcessing();
			}
			gCurrentProcessing = processing;
	  }
	  jstring inFileName = (jstring) env->GetObjectArrayElement(inFiles, 0);
      const char *inFileNameStr = env->GetStringUTFChars(inFileName, 0);
	  //LOGI("--- file name '%s'", inFileNameStr);

      cv::Mat image = cv::imread(inFileNameStr);

      //cv::Mat image = staticImage->clone();

//      if (image.cols > maxWidth && image.rows > maxHeight) {
//		  resize(image, image, cv::Size(maxWidth, maxHeight), 0, 0, cv::INTER_LINEAR);
//	  }
      resizeImage(image, maxWidth, maxHeight);
	  //LOGI("--- IMAGE size %d %d MAX size %d %d image_type=%d CV_8UC3=%d", image.cols, image.rows, maxWidth, maxHeight,
		//	  image.type(), CV_8UC3);


	  // execute presets sequentially
	  LOGI("--- start!! %p", processing);
      if (presets != NULL && env->GetArrayLength(presets) > 0) {
    	  int stringCount = env->GetArrayLength(presets);
    	  for (int i=0; i<stringCount; i++) {
    	    jstring presetName = (jstring) env->GetObjectArrayElement(presets, i);
    	    const char *presetNameStr = env->GetStringUTFChars(presetName, 0);
    	    LOGI("--- doing preset %s", presetNameStr);
    	    std::string presetNameStdString(presetNameStr);
    	    processing->process(image, presetNameStdString);
    	    LOGI("--- done preset %s", presetNameStr);
    	    env->ReleaseStringUTFChars(presetName, presetNameStr);
    	  }
      } else {
    	  processing->process(image);
      }
	  LOGI("--- done!! %p", processing);

	  // save thre result file
	  if (processing->isCancelled()) {
		  LOGI("--- processing!! %p was cancelled", processing);
	  } else {
		  bool res = cv::imwrite(outFileStr, image, getJpegParams(100));
	  }
	  if (processing) {
		  if (processing == gCurrentProcessing) {
			  gCurrentProcessing = 0;
		  }
		  delete processing;
	  }
	  // free matrix memory
	  image.release();
	  env->ReleaseStringUTFChars(inFileName, inFileNameStr);
  }
  env->ReleaseStringUTFChars(outFileName, outFileStr);
  env->ReleaseStringUTFChars(baseAssetPath, baseAssetPathStr);
  
  std::clock_t end = std::clock();
  double elapsed_secs = double(end - begin) / CLOCKS_PER_SEC;
  LOGI("Processing took %f seconds:", elapsed_secs);
} catch (const FilterException & e) {
    //LOGI("Throw filter Exception %s", e.what());
    jclass je = env->FindClass("java/lang/Exception");
    env->ThrowNew(je, e.what());
} catch (...) {
    //LOGI("Throw Unknown exception %s");
    jclass je = env->FindClass("java/lang/Exception");
    env->ThrowNew(je, "Unknown exception in JNI code");
}
}

JNIEXPORT jdoubleArray Java_com_wisesharksoftware_core_ImageProcessing_detectSheetCorners(
          JNIEnv* env,
          jobject obj,
          jstring inFileName)
{
	const char *inFileNameStr = env->GetStringUTFChars(inFileName, 0);
	cv::Mat image = cv::imread(inFileNameStr);

	std::vector<std::vector<cv::Point> > squares;
	std::vector<cv::Point> sheet;
	cv::Mat detMat;
	image.copyTo(detMat);
	SheetDetectionFilter::findSquares(detMat, squares);

	int maxSquareIndex = -1;
	double maxSquare = 0;
	for (int i = 0; i < squares.size(); i++) {
		cv::RotatedRect minRect = minAreaRect(cv::Mat(squares[i]));
		if (maxSquare < (minRect.size.width * minRect.size.height)) {
			maxSquare = (minRect.size.width * minRect.size.height);
			maxSquareIndex = i;
		}
	}

	if (maxSquareIndex == -1) {
		sheet.push_back(cv::Point(-1, -1));
	} else {
		for (int i = 0; i < squares[maxSquareIndex].size(); i++) {
			sheet.push_back(squares[maxSquareIndex][i]);
		}
		//sheet = squares[maxSquareIndex];
	}


	jdoubleArray result;
	int size = sheet.size();
	result = env->NewDoubleArray(size * 2);
	if (result == NULL) {
	   return NULL; /* out of memory error thrown */
	}
	// fill a temp structure to use to populate the java int array
	jdouble temp[size * 2];
	for (int i = 0; i < size; i++) {
	     temp[2 * i] = (sheet[i].x * 1.0) / image.cols; // put whatever logic you want to populate the values here.
	     temp[2 * i + 1] = (sheet[i].y * 1.0) / image.rows;
	}
	// move from the temp structure to the java structure
	env->SetDoubleArrayRegion(result, 0, size * 2, temp);

	image.release();
	return result;

	//return s;
}

JNIEXPORT void Java_com_wisesharksoftware_core_ImageProcessing_combinePhotos(
          JNIEnv* env,
          jobject obj,
          jstring inFileName1, jstring inFileName2, jstring outFileName, int algorithm, int alpha)
{
	const char *inFileNameStr1 = env->GetStringUTFChars(inFileName1, 0);
	cv::Mat image1 = cv::imread(inFileNameStr1);

	const char *inFileNameStr2 = env->GetStringUTFChars(inFileName2, 0);
		cv::Mat image2 = cv::imread(inFileNameStr2);
	const char *outFileNameStr = env->GetStringUTFChars(outFileName, 0);

	if (algorithm == 3) {//ALGORITHM_TRANSPARENCY_ALPHA
		BlendFilter::blendFilterTransparencyOpenCV(image1, image2, image1, alpha);
	} else {
		BlendFilter::blendFilterOpenCV(image1, image2, image1, algorithm);
	}

	std::vector<int> param = std::vector<int>(2);
	param[0] = CV_IMWRITE_JPEG_QUALITY;
	param[1] = 100; //default(95) 0-100
	cv::imwrite(outFileNameStr, image1, param);

	image1.release();
	image2.release();

//	env->ReleaseStringUTFChars(inFileName, inFileStr);
//	env->ReleaseStringUTFChars(outFileName, outFileStr);

}



JNIEXPORT void Java_com_wisesharksoftware_crop_CropTask_cropOpenCV(JNIEnv* env, jobject obj, jstring inFileName,
        jstring outFileName, jint left, jint top, jint right, jint bottom, jint rotate, jboolean mirrorH, jboolean mirrorV){
	LOGI("crop");
	const char* inFileStr = env->GetStringUTFChars(inFileName, 0);
	const char* outFileStr = env->GetStringUTFChars(outFileName, 0);
	// read matrix from file
	cv::Mat raw = cv::imread(inFileStr);
	// crop
	cv::Mat cropped;
	cv::Rect cropRect;
	LOGI("Crop left = %d, top = %d, right = %d, bottom = %d", left, top, right, bottom);
	cropRect = cv::Rect(left, top,right, bottom);
	cv::Mat(raw, cropRect).copyTo(cropped);
	if (&raw != &cropped){
		raw.release();
		raw = cropped;
	}
	cv::Mat rotated;
	int rotHeight = raw.cols;
	int rotWidth = raw.rows;
	if (rotate != 0){
		if (rotate == 90 || rotate == 270){
				rotHeight = raw.rows;
				rotWidth = raw.cols;

		}
		cv::Size rotSz(rotHeight, rotWidth);
		int len;
		if (rotate == 90){
			len = std::min(raw.cols, raw.rows);
		} else {
			len = std::max(raw.cols, raw.rows);
		}




		int cHeight = len/2.;
		int cWidth = len/2.;
		if (rotate == 180){
			cHeight = rotHeight/2.;
			cWidth = rotWidth/2.;
		}
		cv::Point2f center(cHeight, cWidth);
		cv::Mat tmp = cv::getRotationMatrix2D(center, -rotate, 1.0);
		warpAffine(raw, rotated, tmp, rotSz);
		if (&raw != &rotated){
			raw.release();
			raw = rotated;
		}
	}


	if (mirrorH && mirrorV){
		cv::Mat rotated;
		cv::Size rotSz(raw.cols, raw.rows);
		cv::Point2f src_center(raw.cols/2.0F, raw.rows/2.0F);
		cv::Mat rot_mat = getRotationMatrix2D(src_center, 180, 1.0);
		warpAffine(raw, rotated, rot_mat, rotSz);
		raw = rotated;
	}	else{
		if (mirrorH) {
				cv::Mat mirrored;
				cv::flip(raw, mirrored, 0);
				cv::Mat rotated;
				cv::Size rotSz(mirrored.cols, mirrored.rows);
				cv::Point2f src_center(mirrored.cols/2.0F, mirrored.rows/2.0F);
				cv::Mat rot_mat = getRotationMatrix2D(src_center, 180, 1.0);
				warpAffine(mirrored, rotated, rot_mat, rotSz);
				if (&raw != &rotated){
					raw.release();
					raw = rotated;
				}

			}

			if (mirrorV) {
					cv::Mat mirrored;
					cv::flip(raw, mirrored, 0);
					if (&raw != &mirrored){
						raw.release();
						raw = mirrored;
					}
					/*cv::Mat rotated;
					cv::Size rotSz(mirrored.cols, mirrored.rows);
					cv::Point2f src_center(mirrored.cols/2.0F, mirrored.rows/2.0F);
					cv::Mat rot_mat = getRotationMatrix2D(src_center, 180, 1.0);
					warpAffine(mirrored, rotated, rot_mat, rotSz);
					raw = rotated;*/
				}
	}




	// write to file
	std::vector<int> param = std::vector<int>(2);
	param[0] = CV_IMWRITE_JPEG_QUALITY;
	param[1] = 100; //default(95) 0-100
	bool res = cv::imwrite(outFileStr, raw, param);
	raw.release();
	env->ReleaseStringUTFChars(inFileName, inFileStr);
	env->ReleaseStringUTFChars(outFileName, outFileStr);
}

JNIEXPORT jobject JNICALL Java_com_wisesharksoftware_core_AllocationMemory_allocNativeBuffer(JNIEnv* env, jobject thiz, jlong size)
{
    void* buffer = malloc(size);
    jobject directBuffer = env->NewDirectByteBuffer(buffer, size);
    jobject globalRef = env->NewGlobalRef(directBuffer);

    return globalRef;
}

JNIEXPORT void JNICALL Java_com_wisesharksoftware_core_AllocationMemory_freeNativeBuffer(JNIEnv* env, jobject thiz, jobject globalRef)
{
    void *buffer = env->GetDirectBufferAddress(globalRef);

    env->DeleteGlobalRef(globalRef);
    free(buffer);
}

JNIEXPORT void Java_com_wisesharksoftware_core_AllocationMemory_savePhotoOpenCV(
        JNIEnv* env,
        jobject obj,
        jstring fileName,
        bool mirror,
        int angle,
        jobject jpegData,
        int width,
        int height,
        bool square)
{
	unsigned char * cjpeg = (unsigned char*) env->GetDirectBufferAddress(jpegData);
	cv::Mat raw(height, width, CV_8UC4, cjpeg);
	LOGI("--- IMAGE size %d %d image_type=%d CV_8UC4=%d", raw.cols, raw.rows,
			  raw.type(), CV_8UC4);

	int side = std::min(raw.cols, raw.rows);
	bool needRotate = std::abs(angle) == 90 || std::abs(angle) == 270;
	if (needRotate) {
		cv::transpose(raw, raw);
		cv::flip(raw, raw, 1);
	}
	if (square) {
		cv::Rect cropRect(raw.cols / 2 - side / 2, raw.rows / 2 - side / 2, side, side);
		cv::Mat(raw, cropRect).copyTo(raw);
	}

	cv::cvtColor(raw, raw, cv::COLOR_RGBA2BGRA);

	if (mirror) {
		cv::flip(raw, raw, 0);
	}

	const char* fileNameStr = env->GetStringUTFChars(fileName, 0);
	bool res = cv::imwrite(fileNameStr, raw, getJpegParams(100));
	raw.release();
	env->ReleaseStringUTFChars(fileName, fileNameStr);
}

void rotateImage(cv::Mat & image, cv::Mat & rotatedImage, int angle, double scaleW, double scaleH) {
	resize(image, image, cv::Size((int)(image.cols * scaleW), (int)(image.rows * scaleH)), 0, 0, cv::INTER_LINEAR);
	double sinus = fabs(std::sin(angle * 3.14 / 180.0f));
	double cosinus = fabs(std::cos(angle * 3.14 / 180.0f));

	LOGI("--- IMAGE %f %f",
			sinus, cosinus);
	int newWidth = (int) (image.cols * cosinus + image.rows * sinus);
	int newHeight = (int) (image.cols * sinus + image.rows * cosinus);

	cv::Point center(newWidth / 2, newHeight / 2);
	cv::Size targetSize(newWidth, newHeight);

	cv::Mat targetMat(targetSize, image.type());
	targetMat.setTo(cv::Scalar(0, 0, 0, 0));
	int offsetX = (newWidth - image.cols) / 2;
	int offsetY = (newHeight - image.rows) / 2;

	LOGI("--- IMAGE %d %d %d %d %d %d %d",
			image.cols, image.rows, newWidth, newHeight, offsetX, offsetY, angle);
	cv::Mat roi = targetMat(cv::Rect(offsetX, offsetY, image.cols, image.rows));
	image.copyTo(roi);
	LOGI("---1");
	cv::Mat rotImage = cv::getRotationMatrix2D(center, -angle, 1.0);
	LOGI("---2");
	//cv::Mat resultMat = new cv::Mat(); // CUBIC
	warpAffine(targetMat, rotatedImage, rotImage, targetSize, cv::INTER_CUBIC, cv::BORDER_CONSTANT, cv::Scalar::all(0));
	//warpAffine(targetMat, rotatedImage, rotImage, targetSize);
	//rotatedImage = targetMat;
	LOGI("---3");
}
  
#ifdef __cplusplus
}
#endif
