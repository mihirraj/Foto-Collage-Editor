package com.wisesharksoftware.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.smsbackupandroid.lib.ExceptionHandler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

public class AssetsUtils {

	private static AssetManager assetManager;
	private static String basePath;

	public static byte[] readFile(Context context, String srcPath)
			throws IOException {
		InputStream is = context.getAssets().open(srcPath);
		try {
			byte[] fileBytes = new byte[is.available()];
			is.read(fileBytes);
			return fileBytes;
		} finally {
			is.close();
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public static boolean copyAssets(Context context) {
		basePath = context.getExternalFilesDir(null) + "/assets/";
		deleteDirectory(new File(basePath));
		assetManager = context.getAssets();
		return copyAssets("");
	}

	/**
	 * Copies content of AssetsFolder into
	 * /sdcard/Android/data/<namespace>/files/assets
	 * 
	 * @param context
	 */
	@SuppressLint("NewApi")
	private static boolean copyAssets(String dirPath) {
		String[] files = null;
		boolean res = true;
		try {
			//if (dirPath.contains("."))
			//files = assetManager.list(dirPath);

			if (dirPath.contains(".")) {
				boolean resCopy = copyFile(dirPath);
				if (!resCopy) {
					res = false;
				}
			} else {
				files = assetManager.list(dirPath);
				String fullPath = basePath + dirPath;
				File dir = new File(fullPath);
				if (!dir.exists() && !dirPath.startsWith("images")
						&& !dirPath.startsWith("sounds")
						&& !dirPath.startsWith("webkit")
						&& !dirPath.startsWith("kioskmode")) {
					if (!dir.mkdirs()) {
						Log.i("AssetsUtils", "could not create dir " + fullPath);
					}
				}

				for (int i = 0; i < files.length; i++) {
					String p;
					if (dirPath.equals(""))
						p = "";
					else
						p = dirPath + "/";

					if (!dirPath.startsWith("images")
							&& !dirPath.startsWith("sounds")
							&& !dirPath.startsWith("webkit")
							&& !dirPath.startsWith("kioskmode")) 
					{
						boolean resCopy = copyAssets(p + files[i]);
						if (!resCopy) {
							res = false;
						}
					}
				}
			}

		} catch (IOException e) {
			res = false;
			Utils.reportFlurryEvent("Failed to get asset file list", e.toString());
			Log.e("AssetsUtils", "Failed to get asset file list.", e);
			new ExceptionHandler(e, "CopyAssetsError");
		}
		
		return res;

		// pre create folders on sd card
		// File sdCardDir = new File(basePath + "/assets/" + dirPath);
		// if (!sdCardDir.exists()) {
		// sdCardDir.mkdirs();
		// }
		// for (String filename : files) {
		// copyFile(filename);
		// }
	}

	private static boolean copyFile(String filename) {		
		Log.d("AssetsUtils", "Try to copy file: " + filename);
		boolean result = true;
		InputStream in = null;
		OutputStream out = null;
		try {
			File outFile = new File(basePath + filename);

			in = assetManager.open(filename);
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			result = false;			
			Utils.reportFlurryEvent("Failed to copy asset file", e.toString());
			Log.e("AssetsUtils", "Failed to copy asset file: " + filename, e);
		}
		return result;
	}

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	
	public static void deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    path.delete();
	  }
}
