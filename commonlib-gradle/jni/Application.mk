# The ARMv7 is significanly faster due to the use of the hardware FPU
#APP_STL := stlport_static
#APP_ABI := armeabi armeabi-v7a x86

APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions

APP_ABI := armeabi armeabi-v7a
APP_OPTIM := release
APP_PLATFORM := android-8
