package com.wisesharksoftware.gallery;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ImageLoader {

	private String photoFolder;
	
	public ImageLoader(String photoFolder) {
		this.photoFolder = photoFolder;
	}
	
	public void load(Activity activty, Uri imageUri, ImageLoadTask.OnCompleteListener onCompleteListener) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = activty.managedQuery(imageUri, proj, null, null, null);
			if (cursor != null) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String fileName = cursor.getString(column_index);
				if (fileName != null) {
					if (onCompleteListener != null) {
						onCompleteListener.onBitmapReady(fileName);
					}
				} else {
					if (imageUri != null) {
						ImageLoadTask loadTask = new ImageLoadTask();
						loadTask.setTaskData(imageUri.toString(), Utils.getFullFileName(photoFolder, "jpg"), false, activty.getContentResolver());
						loadTask.setOnCompleteListener(onCompleteListener);
						loadTask.execute();
					}
				}
			} else {
				if (onCompleteListener != null) {
					onCompleteListener.onBitmapReady(imageUri.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			FlurryAgent.logEvent("ImageLoaderError");
			new ExceptionHandler(e, "ImageLoaderErorr");
		}
	}
}
