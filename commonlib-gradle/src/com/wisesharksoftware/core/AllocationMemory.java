package com.wisesharksoftware.core;

public class AllocationMemory {
	public static native Object allocNativeBuffer(long size);
	public static native void freeNativeBuffer(Object globalRef);
	public static native void savePhotoOpenCV(String origFileName, boolean mirror, int angle, Object jpegData, int width, int height, boolean square);
}