package com.smsbackupandroid.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.flurry.android.FlurryAgent;

/**
 * Holds different utility methods.
 * Mostly for bitmap operations.
 * @author Roman
 *
 */
public class Utils {
  
  private static void saveBitmap(Bitmap bitmap, File outFile,
                                 Bitmap.CompressFormat format) throws IOException {
    FileOutputStream out = new FileOutputStream(outFile);
    bitmap.compress(format, 100, out);
  }
  
  /**
   * Saves bitmap as PNG
   * @param bitmap
   * @param outFile
   * @throws IOException
   */
	public static void saveBitmap(Bitmap bitmap, File outFile) throws IOException {
	  saveBitmap(bitmap, outFile, Bitmap.CompressFormat.PNG);
	}
	
	/**
	 * Saves bitmap to JPEG file.
	 * @param bitmap Bitmap to save.
	 * @param outFile Path to destination file.
	 * @throws IOException
	 */
	public static void saveBitmapJpeg(Bitmap bitmap, File outFile, boolean recycle) throws IOException {
	  saveBitmap(bitmap, outFile, Bitmap.CompressFormat.JPEG);
	  if (recycle) {
		  bitmap.recycle();
	  }
  }
	
	/**
	 * Sync loads bitmap image from URL.
	 * @param url URL to load from.
	 * @return result Bitmap.
	 * @throws IOException
	 */
	public static Bitmap getImageBitmapFromWeb(String url) throws IOException { 
	  java.net.URL aURL = new java.net.URL(url); 
    java.net.URLConnection conn = aURL.openConnection(); 
    conn.connect(); 
    java.io.InputStream is = conn.getInputStream(); 
    java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is); 
    Bitmap bm = BitmapFactory.decodeStream(bis); 
    bis.close(); 
    is.close(); 
	  return bm; 
	} 
	
  public static Bitmap loadBitmap(File inFile, boolean mutable) throws IOException {
    return loadBitmap(inFile, mutable, 1);
  }
	
  /**
   * Loads bitmap from file.
   * @param inFile path to file.
   * @param mutable should bitmap be mutable.
   * @param inSampleSize image shoudl be smaller by this factor.
   * @return loaded image.
   * @throws IOException
   */
	@SuppressLint("NewApi")
	public static Bitmap loadBitmap(File inFile, boolean mutable, int inSampleSize) throws IOException {
	  System.gc();
		Bitmap bitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		try {
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		} catch(Error e) {
			System.gc();
			e.printStackTrace();
			bitmap = BitmapFactory.decodeFile(inFile.getPath(), options);
		}
		bitmap.setHasAlpha(true);
		if (mutable) {
		  Bitmap mutableBitmap =  bitmap.copy(bitmap.getConfig(), true);
		  bitmap.recycle();
		  return mutableBitmap;
		} else {
		  return bitmap;
		}
	}
	
	/**
	 * Reads file into byte array.
	 * @param file
	 * @return result ByteArray.
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    if (length > Integer.MAX_VALUE) {
        // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int)length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
        throw new IOException("Could not completely read file " +
                              file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
	}
	
	/**
	 * Copies file to another location.
	 * @param fromFile source file.
	 * @param toFile destination file.
	 * @throws IOException
	 */
	public static void copyFile(File fromFile, File toFile) throws IOException { 
    FileInputStream from = null; 
    FileOutputStream to = null; 
    try { 
      from = new FileInputStream(fromFile); 
      to = new FileOutputStream(toFile); 
      byte[] buffer = new byte[4096]; 
      int bytesRead; 
      while ((bytesRead = from.read(buffer)) != -1) {
        to.write(buffer, 0, bytesRead); // write 
      }
      to.flush();
    } finally {
      if (from != null) 
        try { 
          from.close(); 
        } catch (IOException e) { } 
      if (to != null) 
        try { 
          to.close(); 
        } catch (IOException e) { } 
    }
  }
	
	private static final String FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	/**
	 * Creates file name based on current date/time.
	 * @return file name.
	 */
	public static String getDateFileName() {
	  try {
	    Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat(FILE_NAME_FORMAT);
      return sdf.format(cal.getTime());
    }catch (Exception e) {
      return Integer.toHexString(UUID.randomUUID().hashCode());
    }
  }
  
	static public String getFolderPath(String folderName) {
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
  
	static public String getFullFileName(String folderName, String extension) {
	  String name = getFolderPath(folderName) + "/";
	  name += getDateFileName();
    name += "." + extension;
    return name;
  }
	
	static public void showPictureInGallery(final Context ctx, final File pictureFile) {
		addPhotoToGallery(ctx, pictureFile.getAbsolutePath());
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + pictureFile.getAbsolutePath()), "image/*");
		ctx.startActivity(intent);
		//This method only compatible with Android > 2.2
		/*
		 String [] paths = new String[1];
		 paths[0] = pictureFile.getAbsolutePath();
		 MediaScannerConnection.scanFile(ctx, paths, null, 
				 new  MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String arg0, Uri arg1) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file://" + pictureFile.getAbsolutePath()), "image/*");
						ctx.startActivity(intent);
					}
		 });
		*/
	}
	
	static public void addPhotoToGallery(Context ctx, String filePath) {
		Uri uri = Uri.parse("file://" + filePath);
		ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	}
	
	static public void deletePhotoFromGallery(Context ctx, String filePath){
		Uri uri = Uri.parse("file://" + filePath);
		ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	}
	
	static public void reportFlurryEvent(String eventId, String paramValue) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("0", paramValue);
		FlurryAgent.logEvent(eventId, params);
	}
}