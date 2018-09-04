package com.wisesharksoftware.core;

import com.smsbackupandroid.lib.ExceptionHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

public class OrientationListener extends OrientationEventListener {

	public static final int ROTATION_0 = 0;
	public static final int ROTATION_90 = 1;
	public static final int ROTATION_180 = 2;
	public static final int ROTATION_270 = 3;
	
	public static final int ORIENTATION_LANDSCAPE = 0;
	public static final int ORIENTATION_PORTRAIT = 1;
	
	private int currentOrientation = ROTATION_0;
	private int defaultOrientation = Configuration.ORIENTATION_PORTRAIT; 
	
	public OrientationListener(Context context, int rate) {
		super(context, rate);
		try {
			defaultOrientation = getDeviceDefaultOrientation(context);
		} catch (Exception e) {
			defaultOrientation = Configuration.ORIENTATION_PORTRAIT;
			e.printStackTrace();
			new ExceptionHandler(e, "OrientationListener");
		}
	}

	@Override
	public void onOrientationChanged(int angle) {
		if (angle > 315) {
			currentOrientation = ROTATION_0;
		}
		if (angle < 45) {
			currentOrientation = ROTATION_0;
		}
		if (angle > 45 && angle < 135) {
			currentOrientation = ROTATION_90;
		}
		if (angle > 135 && angle < 225) {
			currentOrientation = ROTATION_180;
		}
		if (angle > 225 && angle < 315) {
			currentOrientation = ROTATION_270;
		}
	}
	
	public int getRotationDegrees() {
		if (defaultOrientation == Configuration.ORIENTATION_PORTRAIT) {
			return currentOrientation == ROTATION_0 || currentOrientation == ROTATION_180 ? 90 : 0;
		} else {
			return currentOrientation == ROTATION_90 || currentOrientation == ROTATION_270 ? 90 : 0;
		}
	}
	
	@SuppressLint("NewApi")
	public int getDeviceDefaultOrientation(Context context){
		try {
			WindowManager lWindowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

			Configuration cfg = context.getResources().getConfiguration();
		
			int lRotation = lWindowManager.getDefaultDisplay().getRotation();

			if( (((lRotation == Surface.ROTATION_0) ||(lRotation == Surface.ROTATION_180)) &&   
					(cfg.orientation == Configuration.ORIENTATION_LANDSCAPE)) ||
					(((lRotation == Surface.ROTATION_90) ||(lRotation == Surface.ROTATION_270)) &&    
					(cfg.orientation == Configuration.ORIENTATION_PORTRAIT))){
				return Configuration.ORIENTATION_LANDSCAPE;
			}     
			return Configuration.ORIENTATION_PORTRAIT;
		} catch (Error e) {
			new ExceptionHandler(e, "getDeviceDefaultOrientation");
			return Configuration.ORIENTATION_PORTRAIT;
		}
	}

}
