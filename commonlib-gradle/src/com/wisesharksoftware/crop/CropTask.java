package com.wisesharksoftware.crop;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.Utils;

import android.graphics.Rect;
import android.os.AsyncTask;

public class CropTask extends AsyncTask<Object, Integer, Boolean> {

	private final TouchImageView image;
	private final String input;
	private final String output;
	private String LOG_TAG = "CropTask";
	
	public CropTask(TouchImageView image, String input, String output) {
		this.image = image;
		this.input = input;
		this.output = output;
	}
	
	@Override
	protected Boolean doInBackground(Object... arg0) {
		Rect rect = null;
		int rotate = 0;
		boolean mirrorH = false;
		boolean mirrorV = false;
		try{
			rect = image.getImageRect();
			rotate = image.getRotate();
			mirrorH = image.getMirrorH();
			mirrorV = image.getMirrorV();
			cropOpenCV(input, output, rect.left, rect.top, rect.right,
					rect.bottom, rotate, mirrorH, mirrorV);
			return true;
		} catch (Exception ex){
			String msg = (rect != null ? ("r:" + rect.left + ";" +  rect.top + ";" +  rect.right + ";" + rect.bottom) : "r:null;") + "m:" + mirrorH + ";" + mirrorV + 
					"s:" + image.getImageWidth() + "x" + image.getImageHeight();
			Utils.reportFlurryEvent("crop exception", msg);
			new ExceptionHandler(ex, "CropError");
			return false;
		}
	}

	public static native void cropOpenCV(String input, String output, int left,
			int top, int right, int bottom, int rotate, boolean mirrorH,
			boolean mirrorV);	
}
