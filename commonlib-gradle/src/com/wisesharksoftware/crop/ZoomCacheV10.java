package com.wisesharksoftware.crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.TargetApi;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;

public class ZoomCacheV10 extends ZoomCache {

	public ZoomCacheV10(float preZoom, float zoom, RectF cacheBounds) {
		super(preZoom, zoom, cacheBounds);
	}

	@TargetApi(10)
	@Override
	public void loadCache(String picturePath) {
		try {

			FileInputStream istream = new FileInputStream(new File(picturePath));
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(istream, false);

			BitmapFactory.Options options = new BitmapFactory.Options();
			int sampleSize = (int) (1 / (workZoom * preZoom));
			
			if (0 == sampleSize)
				sampleSize = 1;

			options.inSampleSize = sampleSize;

			Rect bounds = new Rect((int) zoomCacheBounds.left, (int) zoomCacheBounds.top,
						(int) zoomCacheBounds.right, (int) zoomCacheBounds.bottom);

			bitmap = decoder.decodeRegion(bounds, options);

			// calculate real zoom after resampling //
			cacheZoom = bitmap.getWidth() / (zoomCacheBounds.right - zoomCacheBounds.left);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
