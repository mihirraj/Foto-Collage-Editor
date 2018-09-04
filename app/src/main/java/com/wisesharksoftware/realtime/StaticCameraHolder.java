package com.wisesharksoftware.realtime;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;

import android.hardware.Camera;

public class StaticCameraHolder {

	private static Camera camera;
	
	public static void setCamera(Camera c) {
		camera = c;
	}
	
	public static synchronized void releaseCamera() {
		try {
			if (camera != null) {
				FlurryAgent.logEvent("StaticCameraHolder_ReleaseCamera");
				camera.stopPreview();
				camera.setPreviewCallback(null);
				camera.release();
				camera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "StaticCameraHolder_ReleaseCamera");
		}
	}
}
