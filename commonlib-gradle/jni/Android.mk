LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# OpenCV
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include ../../opencv/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := processing
LOCAL_LDLIBS    := -lm -llog

LOCAL_CPPFLAGS := -O3 -DNDEBUG

#LOCAL_CPPFLAGS := -O3 -flto -ffunction-sections -fdata-sections
#LOCAL_CPPFLAGS += -fvisibility=hidden -fvisibility-inlines-hidden 
#LOCAL_CPPFLAGS += -fomit-frame-pointer -funroll-loops -ffast-math
#LOCAL_CPPFLAGS += -DFPM_ARM -DNDEBUG

LOCAL_SRC_FILES := jsonxx.cpp \
                    Filter.cpp \
					FishEyeFilter.cpp \
					AnimalEyesFilter.cpp \
					BlendFilter.cpp \
					BillboardFilter.cpp \
					ColorizeHsvFilter.cpp \
					HsvFilter.cpp \
					SimpleGlitchFilter.cpp \
					GlitchFilter.cpp \
					CurveFilter.cpp \
					utils.cpp \
					ImageProcessing.cpp \
					BitFilter.cpp \
					PosterizeFilter.cpp \
					ThresholdFilter.cpp \
					GrayFilter.cpp \
					ComicFilter.cpp \
					EdgeFilter.cpp \
					CropRectangleFilter.cpp \
					SquareFilter.cpp \
					SquareBorderFilter.cpp \
					TiltShiftFilter.cpp \
					MultiPicturesFilter.cpp \
					CombinePicturesFilter.cpp \
					MultipleScenesFilter.cpp \
					CBFilter.cpp \
					HDRFilter.cpp \
					HDRFilter2.cpp \
					CoarseEdgesFilter.cpp \
					DilationFilter.cpp \
					FlipRotateFilter.cpp \
					ThresholdBlurFilter.cpp \
					EdgeClosingFilter.cpp \
					ClosingFilter.cpp \
					SharpenFilter.cpp \
					StickerFilter.cpp \
					ColorTemperatureFilter.cpp \
					CropFilter.cpp \
					MirrorFilter.cpp \
					FocusFilter.cpp \
					FaceDetectionFilter.cpp \
					SheetDetectionFilter.cpp \
					PerspectiveTransformFilter.cpp \
					SaveImageFilter.cpp \
					ConvolutionFilter.cpp \
					SharpnessFilter.cpp \
					InkFilter.cpp \
					config.cpp \
					processing.cpp

include $(BUILD_SHARED_LIBRARY)
