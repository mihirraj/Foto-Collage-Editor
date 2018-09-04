package com.wisesharksoftware.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;

public class Utils {
	private static final String FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss";

	public static int dpToPix(int dp, Context context) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}

	@TargetApi(4)
	public static Bitmap loadBitmap(File inFile, boolean multiPictures,
			boolean squared, boolean mutable) throws IOException {
		return loadBitmap(inFile, multiPictures, squared, mutable,
				GlobalSettings.getImageWidth(squared, multiPictures),
				GlobalSettings.getImageHeight(squared, multiPictures));
	}

	public static String copy(String in, String out, String name)
			throws IOException {
		Log.v("Copy In", in);
		Log.v("Copy Out", out);
		File src = new File(in);
		File dst = new File(out);
		dst.mkdirs();
		dst = new File(out + "/" + name);
		dst.createNewFile();
		FileInputStream inStream = new FileInputStream(src);
		FileOutputStream outStream = new FileOutputStream(dst);
		FileChannel inChannel = inStream.getChannel();
		FileChannel outChannel = outStream.getChannel();
		inChannel.transferTo(0, inChannel.size(), outChannel);
		inStream.close();
		outStream.close();
		return out;
	}

	@SuppressLint("NewApi")
	@TargetApi(4)
	public static Bitmap loadBitmap(File inFile, boolean multiPictures,
			boolean squared, boolean mutable, int reqWidth, int reqHeight)
			throws IOException {

		System.gc();
		Bitmap bitmap;

		// do not load very big images, trying to scale them fist as much as
		// possible
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		BitmapFactory.decodeFile(inFile.getPath(), options);

		int height = options.outHeight;
		int width = options.outWidth;

		if (height < reqHeight && width < reqWidth) {
			reqHeight = height;
			reqWidth = width;
		}
		// int reqWidth = GlobalSettings.getImageWidth( squared, multiPictures
		// );
		// int reqHeight = GlobalSettings.getImageHeight( squared, multiPictures
		// );

		// if image is portrait need to switch required width and height
		if (height > width) {
			int tmp = reqHeight;
			reqHeight = reqWidth;
			reqWidth = tmp;
		}

		// load image scaled as much as possible to the desired size
		int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inDither = true;
		options.inScaled = false;
		try {
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		} catch (Error e) {
			System.gc();
			e.printStackTrace();
			new ExceptionHandler(e, "DecodeFile");
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		}
		bitmap.setHasAlpha(true);
		// scaling and cropping the loaded image further
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		// cropping
		float reqRatio = (float) reqWidth / reqHeight;
		float ratio = (float) width / height;
		float k = ratio / reqRatio;
		int widthCropped = width;
		int heightCropped = height;
		int left = 0;
		int top = 0;
		if (k > 1) {
			widthCropped = (int) (width / k);
			left = (width - widthCropped) / 2;
		} else {
			heightCropped = (int) (height * k);
			top = (height - heightCropped) / 2;
		}

		float scale = Math.max((float) reqWidth / width, (float) reqHeight
				/ height);

		System.gc();
		// crop (fix width/height ratio)
		Bitmap croppedBitmap = null;
		if (widthCropped != width || heightCropped != height) {
			croppedBitmap = Bitmap.createBitmap(bitmap, left, top,
					widthCropped, heightCropped);
			bitmap.recycle();
			System.gc();
		} else {
			croppedBitmap = bitmap;
		}
		// scale (to match reqWidth/reqHeight)
		if (scale < 1) {
			boolean filter = true;
			bitmap = Bitmap.createScaledBitmap(croppedBitmap, reqWidth,
					reqHeight, filter);
			croppedBitmap.recycle();
			System.gc();
		} else {
			bitmap = croppedBitmap;
			croppedBitmap = null;
		}

		if (mutable) {
			Bitmap mutableBitmap = bitmap.copy(bitmap.getConfig(), true);
			bitmap.recycle();
			return mutableBitmap;
		} else {
			return bitmap;
		}
	}

	public static Bitmap loadBitmapPortrait(File inFile, boolean multiPictures,
			boolean squared, boolean mutable) throws IOException {
		System.gc();
		Bitmap bitmap;

		// do not load very big images, trying to scale them fist as much as
		// possible
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(inFile.getPath(), options);

		int height = options.outHeight;
		int width = options.outWidth;

		int reqHeight = GlobalSettings.getImageWidth(squared, multiPictures);
		int reqWidth = GlobalSettings.getImageHeight(squared, multiPictures);

		// load image scaled as much as possible to the desired size
		int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		try {
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		} catch (Error e) {
			System.gc();
			e.printStackTrace();
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		}

		// scaling and cropping the loaded image further
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		Matrix mtrx = new Matrix();
		float scale = Math.max((float) reqWidth / width, (float) reqHeight
				/ height);
		mtrx.postScale(scale, scale);
		Bitmap tmpBitmap = bitmap;
		System.gc();
		bitmap = Bitmap.createBitmap(bitmap, (int) (scale
				* (width - reqWidth / scale) / 2), (int) (scale
				* (height - reqHeight / scale) / 2),
				(int) Math.ceil(reqWidth / scale),
				(int) Math.ceil(reqHeight / scale), mtrx, false);
		tmpBitmap.recycle();

		if (mutable) {
			Bitmap mutableBitmap = bitmap.copy(bitmap.getConfig(), true);
			bitmap.recycle();
			return mutableBitmap;
		} else {
			return bitmap;
		}
	}

	public static Bitmap loadBitmapLandscape(File inFile,
			boolean multiPictures, boolean squared, boolean mutable)
			throws IOException {
		System.gc();
		Bitmap bitmap;

		// do not load very big images, trying to scale them fist as much as
		// possible
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(inFile.getPath(), options);

		int height = options.outHeight;
		int width = options.outWidth;

		int reqWidth = GlobalSettings.getImageWidth(squared, multiPictures);
		int reqHeight = GlobalSettings.getImageHeight(squared, multiPictures);

		// load image scaled as much as possible to the desired size
		int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		try {
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		} catch (Error e) {
			System.gc();
			e.printStackTrace();
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		}

		// scaling and cropping the loaded image further
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		Matrix mtrx = new Matrix();
		float scale = Math.max((float) reqWidth / width, (float) reqHeight
				/ height);
		mtrx.postScale(scale, scale);
		Bitmap tmpBitmap = bitmap;
		System.gc();
		bitmap = Bitmap.createBitmap(bitmap, (int) (scale
				* (width - reqWidth / scale) / 2), (int) (scale
				* (height - reqHeight / scale) / 2),
				(int) Math.ceil(reqWidth / scale),
				(int) Math.ceil(reqHeight / scale), mtrx, false);
		tmpBitmap.recycle();

		if (mutable) {
			Bitmap mutableBitmap = bitmap.copy(bitmap.getConfig(), true);
			bitmap.recycle();
			return mutableBitmap;
		} else {
			return bitmap;
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = reqHeight >= 1200 ? (int) Math
						.ceil((double) height / (double) reqHeight)
						: (int) Math
								.floor((double) height / (double) reqHeight);
			} else {
				inSampleSize = reqWidth >= 1200 ? (int) Math
						.ceil((double) width / (double) reqWidth) : (int) Math
						.floor((double) width / (double) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static int calculateThambnailInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = (int) Math.ceil((double) height
						/ (double) reqHeight);
			} else {
				inSampleSize = (int) Math.ceil((double) width
						/ (double) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static String getStringAsset(Context context, String asset,
			Object... args) throws IOException {
		InputStream input = context.getResources().getAssets().open(asset);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuffer buf = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buf.append(line);
		}
		String aboutText = buf.toString();
		aboutText = String.format(aboutText, args);
		aboutText.replaceAll("percent", "%");
		return aboutText;
	}

	public static Bitmap getBitmapAsset(Context context, String asset,
			boolean hd) throws IOException {
		System.gc();
		InputStream imgFile = context.getAssets().open(
				(hd ? "hd/" : "sd/") + asset);
		return BitmapFactory.decodeStream(imgFile);
	}

	public static void saveBitmapJpeg(Bitmap bitmap, File outFile,
			boolean recycle) throws IOException {
		saveBitmap(bitmap, outFile, Bitmap.CompressFormat.JPEG);
		if (recycle) {
			bitmap.recycle();
			System.gc();
		}
	}

	public static String getFullFileName(String folderName, String extension) {
		String name = getFolderPath(folderName) + "/";
		name += getDateFileName();
		name += "." + extension;
		return name;
	}

	public static String getFullFileName(String folderName, String fileName,
			String extension) {
		String name = getFolderPath(folderName) + "/";
		name += fileName;
		name += "." + extension;
		return name;
	}

	public static String getFolderPath(String folderName) {
		if (folderName != null) {
			File rootFolder = Environment.getExternalStorageDirectory();
			File folderPath = new File(rootFolder, folderName);
			if (!folderPath.exists()) {
				folderPath.mkdirs();
			}
			return folderPath.getAbsolutePath();
		}
		return null;
	}

	public static String getDateFileName() {
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(FILE_NAME_FORMAT);
			return sdf.format(cal.getTime());
		} catch (Exception e) {
			return Integer.toHexString(UUID.randomUUID().hashCode());
		}
	}

	public static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
		System.gc();
		Matrix mtrx = new Matrix();
		mtrx.postRotate(degrees);
		Bitmap beforeRotate = bitmap;
		bitmap = Bitmap.createBitmap(beforeRotate, 0, 0,
				beforeRotate.getWidth(), beforeRotate.getHeight(), mtrx, true);
		beforeRotate.recycle();
		return bitmap;
	}

	@SuppressLint("NewApi")
	public static Bitmap getThumbnailFromPath(String filePath, int maxWidth,
			int maxHeight) {
		System.gc();
		// first, get image size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inJustDecodeBounds = false;
		int inSampleSize = calculateThambnailInSampleSize(options, maxWidth,
				maxHeight);
		if (inSampleSize > 1) {
			options.inSampleSize = inSampleSize;
		}
		options.inPreferredConfig = Config.RGB_565;
		options.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		if (inSampleSize <= 1) {
			return bitmap;
		}
		if (bitmap == null) {
			return null;
		}
		Matrix mtrx = new Matrix();
		float scale = Math.min((float) maxWidth / bitmap.getWidth(),
				(float) maxHeight / bitmap.getHeight());
		mtrx.postScale(scale, scale);

		System.gc();
		if (scale != 1.0f) {
			Bitmap tmpBitmap = bitmap;
			bitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(),
					tmpBitmap.getHeight(), mtrx, true);
			tmpBitmap.recycle();
			return bitmap;

		} else {
			return bitmap;
		}
		// return Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(),
		// bitmap.getHeight(), mtrx, true );
	}

	static public void addPhotoToGallery(Context ctx, String filePath) {
		Uri uri = Uri.parse("file://" + filePath);
		ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
				.setData(uri));
		Log.d("check", "onSuccess: saving");
	}

	static public void addPhotoToGallery(Context ctx, Uri uri) {
		ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
				.setData(uri));
	}

	private static void saveBitmap(Bitmap bitmap, File outFile,
			Bitmap.CompressFormat format) throws IOException {
		FileOutputStream out = new FileOutputStream(outFile);
		bitmap.compress(format, 100, out);
		out.close();
	}

	static public void reportFlurryEvent(String eventId, String paramValue) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("0", paramValue);
		FlurryAgent.logEvent(eventId, params);
	}

	static public BitmapSize getBitmapSizeFromFile(String filePath) {
		// Get ratio from file
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inJustDecodeBounds = false;
		float width = options.outWidth;
		float height = options.outHeight;
		return new BitmapSize((int) width, (int) height);
	}

	static public Drawable getAndroidDrawable(String pDrawableName) {
		int resourceId = Resources.getSystem().getIdentifier(pDrawableName,
				"drawable", "android");
		if (resourceId == 0) {
			return null;
		} else {
			return Resources.getSystem().getDrawable(resourceId);
		}
	}

}
