package com.smsbackupandroid.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AssetsAdapter {
	
  public static String getAsset(Context context, String asset, 
			Object ...args) throws IOException {
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
	
	public static Bitmap getBitmap(Context context, String asset) throws IOException {
	  System.gc();
	  //InputStream input = context.getResources().getAssets().open(asset);
	  //FlushedInputStream inputStream = new FlushedInputStream(input);
	  //afd.getFileDescriptor().
	  //File f = new File("file:///android_asset/"+asset);
	  //return Utils.loadBitmap(f, false);

//	  AssetFileDescriptor afd = context.getAssets().openFd(asset);
//	  return BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor());
	  InputStream imgFile = context.getAssets().open(asset);
	  return BitmapFactory.decodeStream(imgFile);
	}
	
	static class FlushedInputStream extends FilterInputStream {
    public FlushedInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public long skip(long n) throws IOException {
        long totalBytesSkipped = 0L;
        while (totalBytesSkipped < n) {
            long bytesSkipped = in.skip(n - totalBytesSkipped);
            if (bytesSkipped == 0L) {
                  int bt = read();
                  if (bt < 0) {
                      break;  // we reached EOF
                  } else {
                      bytesSkipped = 1; // we read one byte
                  }
           }
            totalBytesSkipped += bytesSkipped;
        }
        return totalBytesSkipped;
    }
	}
}