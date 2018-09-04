package com.wisesharksoftware.gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import com.smsbackupandroid.lib.ExceptionHandler;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoadTask extends AsyncTask<String, Void, Void> {

	private String errorMessage = null;
	private boolean outOfMemory;
	OnCompleteListener listener = null;
	String srcPath;
	String workFilePath;
	boolean unlinkSrc;
	ContentResolver contentResolver;
	
	public void setTaskData(String src, String dst, boolean unlinkSrc, ContentResolver contentResolver) {

		// source picture path/url //
    	this.srcPath = src;

    	// target picture path //
    	this.workFilePath = dst;

    	// null means keep source file untouched //
    	// not null means delete source file //
    	this.unlinkSrc = unlinkSrc;
    	
    	this.contentResolver = contentResolver;
	}

	public void setOnCompleteListener(OnCompleteListener listener) {
		this.listener = listener;
	}

	@Override
    protected Void doInBackground(String... params) {

		try {
    		moveFile(srcPath, workFilePath, unlinkSrc, contentResolver);

    		//tbd.path = workFilePath;

//			ExifInterface exif = new ExifInterface(tbd.path);
//			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//			switch (orientation) {
//	
//			case ExifInterface.ORIENTATION_ROTATE_90:
//				tbd.defaultAngle = 1;
//				break;
//			case ExifInterface.ORIENTATION_ROTATE_180:
//				tbd.defaultAngle = 2;
//				break;
//			case ExifInterface.ORIENTATION_ROTATE_270:
//				tbd.defaultAngle = 3;
//				break;
//			default:
//				tbd.defaultAngle = 0;
//					break;
//			}

			// get captured picture dimensions //
//			BitmapFactory.Options bitmapOptions;
//	
//			bitmapOptions = new BitmapFactory.Options();
//	
//			bitmapOptions.inJustDecodeBounds = true;
//		    BitmapFactory.decodeFile(tbd.path, bitmapOptions);
//	
//		    tbd.rawWidth = bitmapOptions.outWidth;
//		    tbd.rawHeight = bitmapOptions.outHeight;
//
//		    if ((0 >= tbd.rawWidth) || (0 >= tbd.rawHeight))
//		    	throw new IOException("Could not decode image data");
//	
//		    // load picture scaling it to given dimensions //
//			bitmapOptions.inJustDecodeBounds = false;
//	
//		    int widthRatio = (int) ((float) tbd.rawWidth / (float) tbd.normWidth);
//		    if (0 == widthRatio)
//		    	widthRatio = 1;
//	
//		    int heightRatio = (int) ((float) tbd.rawHeight / (float) tbd.normHeight);
//		    if (0 == heightRatio)
//		    	heightRatio = 1;
//	
//		    bitmapOptions.inSampleSize = (heightRatio < widthRatio) ? heightRatio : widthRatio;
//	
//			tbd.mBitmap = BitmapFactory.decodeFile(tbd.path, bitmapOptions);
//			if (null == tbd.mBitmap)
//				throw new IOException("Could not decode image data");
//	
//			tbd.preZoom = (float) tbd.mBitmap.getWidth() / (float) tbd.rawWidth;

		} catch (IOException e) {
			e.printStackTrace();
    		errorMessage = e.getMessage();
    		new ExceptionHandler(e, "ImageLoadTaskError");
		} catch (OutOfMemoryError e) {
			outOfMemory = true;
			e.printStackTrace();
    		new ExceptionHandler(e, "ImageLoadTaskError");
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
    		new ExceptionHandler(e, "ImageLoadTaskError");
		}

		return null;
	}
    @Override
    protected void onPreExecute() {
    	if (null != listener) {
    		listener.onStartLoad();
    	}
    }

    @Override
    protected void onPostExecute(final Void unused) {

    	if ((null != errorMessage) || (outOfMemory)) {

    		if (null != listener)
    			listener.onBitmapLoadError(srcPath, outOfMemory, errorMessage);
    		return;
    	}

    	if (null != listener)
			listener.onBitmapReady(workFilePath);
    }

    public interface OnCompleteListener {
    	public void onStartLoad();
    	public void onBitmapReady(String picturePath);
    	public void onBitmapLoadError(String picturePath, boolean outOfMemory,
    			String errorMessage);
    }
    
	public static void moveFile(String from, String to, boolean unlinkSrc,
			ContentResolver contentResolver) throws IOException
    {
		String errorMessage = null;

		try {
			new URL(from);
			getFileFromURI(from, to, contentResolver);
			return;
		} catch (MalformedURLException e) {
    	}

		if (from.startsWith("content://")) {
			getFileFromURI(from, to, contentResolver);
			return;
		}

		File sourceFile = new File(from);
        FileInputStream fis = null;
        FileChannel in = null;

        File targetFile = new File(to);
        FileOutputStream fos = null;
        FileChannel out = null;

        try
        {
            targetFile.createNewFile();

            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            in = fis.getChannel();
            out = fos.getChannel();

            long size = in.size();
            in.transferTo(0, size, out);
        }
        catch (Throwable e)
        {
            errorMessage = e.getMessage();
        }
        finally
        {
            try
            {
                if (fis != null)
                    fis.close();
            }
            catch (Throwable ignore)
            {}

            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (Throwable ignore)
            {}

            try
            {
                if (in != null && in.isOpen())
                    in.close();
            }
            catch (Throwable ignore)
            {}

            try
            {
                if (out != null && out.isOpen())
                    out.close();
            }
            catch (Throwable ignore)
            {}
        }

        if (unlinkSrc)
        	sourceFile.delete();

        if (null != errorMessage)
        	throw new IOException(errorMessage);
    }

	public static void getFileFromURI(String src, String to,
				ContentResolver contentResolver) throws IOException
    {
		URL srcUrl;
		URLConnection uc = null;
		InputStream in = null;
		File targetFile = null;
        FileOutputStream fos = null;
        Uri uri = Uri.parse(src);
        String errorMessage = null;

		if (src.startsWith("content://")) {
			in = contentResolver.openInputStream(uri);
		} else {

	        try {
				srcUrl = new URL(src);
	    		uc = srcUrl.openConnection();
	    		in = uc.getInputStream();
			} catch (MalformedURLException e) {
				return;
			}
		}

        try
        {

            targetFile = new File(to);

            byte[] buffer = new byte[4096];
            targetFile.createNewFile();

            fos = new FileOutputStream(targetFile);

            int bytesRead = -1;
            while((bytesRead = in.read(buffer)) > -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        catch (Throwable e)
        {
            errorMessage = e.getMessage();
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (Throwable ignore)
            {}

            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (Throwable ignore)
            {}

            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (Throwable ignore)
            {}
        }

        if (null != errorMessage)
        	throw new IOException(errorMessage);
    }

}
