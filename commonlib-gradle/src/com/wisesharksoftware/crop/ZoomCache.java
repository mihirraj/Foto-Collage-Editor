package com.wisesharksoftware.crop;

import android.graphics.Bitmap;
import android.graphics.RectF;

/*
 * Base zoom cache class for use on API versions prior 10
 * where no BitmapRegionDecoder, so no zoom cache available 
 */
public class ZoomCache {

	public final static long ZOOM_LOAD_DELAY = 750;

	// bitmap of zoomed part of image //
	Bitmap bitmap = null;

	float preZoom;
	float workZoom;

	// coordinates of zoomed area inside raw picture // 
	RectF zoomCacheBounds = null;

	// zoom change relative to original picture size //
	float cacheZoom = 1;

	public ZoomCache(float preZoom, float zoom, RectF cacheBounds) {
		zoomCacheBounds = new RectF(cacheBounds);
		this.preZoom = preZoom;
		this.workZoom = zoom;
	}

	public void loadCache(String picturePath) {
	}
}
