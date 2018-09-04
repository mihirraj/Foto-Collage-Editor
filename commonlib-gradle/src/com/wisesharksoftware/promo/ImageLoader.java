package com.wisesharksoftware.promo;

import java.io.FileOutputStream;
import java.io.InputStream;

import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
	private Context context;
	private static final String LOG_TAG = "ImageLoader";
	private String filepath;
	private String id;

	public ImageLoader(Context context) {
		this.context = context;
		if (context != null){
			filepath = new StringBuilder()
			.append(context.getCacheDir().getAbsolutePath()).append("/")
			.append(PromoLoader.PROMO_FILE_NAME).toString();
		}
	}

	protected Bitmap doInBackground(String... urls) {
		if (context == null){
			return null;
		}
		String urldisplay = urls[0];
		Bitmap bitmap = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			bitmap = BitmapFactory.decodeStream(in);
			// check image exists
			FileOutputStream out = new FileOutputStream(filepath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			if (bitmap != null) {
				bitmap.recycle();
				System.gc();
			}
			if (id != null) {
				Utils.reportFlurryEvent(id, "Download");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	protected void onPostExecute(Bitmap result) {
		try {
			if (result == null){
				return;
			}
			SettingsHelper.setBoolean(context, PromoLoader.KEY_LOAD_COMPLETED,
					true);
			Log.d("test", " bitmap loaded " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}