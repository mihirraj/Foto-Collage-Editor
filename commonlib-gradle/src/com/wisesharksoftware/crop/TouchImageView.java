/*
 * TouchImageView.java
 * By: Michael Ortiz
 * Updated By: Patrick Lackemacher
 * Updated By: Babay88
 * -------------------
 * Extends Android ImageView to include pinch zooming and panning.
 */

package com.wisesharksoftware.crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.wisesharksoftware.core.BitmapSize;
import com.wisesharksoftware.core.Utils;

public class TouchImageView extends ImageView {
	private Rect bounds;
	Bitmap bitmap;
	Matrix matrix;
	Container container;
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF last = new PointF();
	PointF start = new PointF();
	float minScale = 1f;
	float maxScale = 3f;
	float[] m;

	float viewWidth, viewHeight;
	static final int CLICK = 3;
	float saveScale = 1f;
	protected float origWidth, origHeight;
	int oldMeasuredWidth, oldMeasuredHeight;

	private boolean large;
	ScaleGestureDetector mScaleDetector;

	Context context;

	private int desiredWidth;
	private int desiredHeight;

	private String LOG_TAG = "TouchImageView";

	private float rectLeft;

	private float rectTop;

	private float rectWidth;

	private float rectHeight;

	// source image size
	private float initScale;

	private int rotate;
	private Bitmap thumb;
	private float ratio;
	private String input;

	private boolean doMirrorV;
	private boolean doMirrorH;
	private boolean mirrorV;
	private boolean mirrorH;
	private boolean doRotate;

	private float maxBitmapWidth = 0;
	private float maxBitmapHeight = 0;
	private boolean dragReported = false;
	private boolean zoomReported = false;

	public float getImageWidth(){
		return maxBitmapWidth;
	}
	
	public float getImageHeight(){
		return maxBitmapHeight;
	}
	
	float containerRatio;

	public TouchImageView(Context context) {
		super(context);
		sharedConstructing(context);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructing(context);
	}

	public Rect getImageRect() {
		fixTrans();
		loadRect(new Canvas());
		int left = bounds.left;
		Log.d(LOG_TAG, "bounds.left = " + bounds.left);
		int top = bounds.top;
		int width = bounds.width();
		int height = bounds.height();

		// check square ratio
		if (viewWidth == viewHeight) {
			if (width != height) {
				int a = Math.min(width, height);
				width = a;
				height = a;
			}
		}

		if (left + width > maxBitmapWidth) {
			Log.d(LOG_TAG, "left + width > maxBitmapWidth" + " old left = "
					+ left);
			left -= (left + width) - maxBitmapWidth;
			Log.d(LOG_TAG, "left + width > maxBitmapWidth" + " new left = "
					+ left);
		}
		if (top + height > maxBitmapHeight) {
			Log.d(LOG_TAG, "top + height > maxBitmapHeight" + " old top = "
					+ top);
			top -= (top + height) - maxBitmapHeight;
			Log.d(LOG_TAG, "top + height > maxBitmapHeight" + " new top = "
					+ top);
			// rectHeight = maxBitmapHeight;
		}

		Log.d(LOG_TAG, "cropping area: " + "left = " + left + " top = " + top
				+ " width = " + width + " height = " + height);

		Rect bounds2 = new Rect(left, top, width,
				height);
		return bounds2;

	}

	@SuppressLint("NewApi")
	private void sharedConstructing(Context context) {
		super.setClickable(true);
		this.context = context;
		zoomReported = false;
		dragReported = false;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix = new Matrix();
		m = new float[9];
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);

		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mScaleDetector.onTouchEvent(event);
				PointF curr = new PointF(event.getX(), event.getY());

				Log.d(LOG_TAG, m[0] + " " + m[1] + " " + m[2] + " " + m[3]
						+ " " + m[4] + " " + m[5] + " " + m[6] + " " + m[7]
						+ " " + m[8] + " ");
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					last.set(curr);
					start.set(last);
					mode = DRAG;
					if (!dragReported) {
						Log.d(LOG_TAG, "DARG");
						Utils.reportFlurryEvent("Crop", "Drag");
						dragReported = true;
					}
					break;

				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						float deltaX = curr.x - last.x;
						float deltaY = curr.y - last.y;
						Log.d(LOG_TAG, "deltaX = " + deltaX);
						Log.d(LOG_TAG, "deltaY = " + deltaY);
						Log.d(LOG_TAG, "saveScale = " + saveScale);
						Log.d(LOG_TAG, "origWidth = " + origWidth
								+ " origHeight = " + origHeight);

						float fixTransX = getFixDragTrans(deltaX, viewWidth,
								origWidth * saveScale);

						float fixTransY = getFixDragTrans(deltaY, viewHeight,
								origHeight * saveScale);
						matrix.postTranslate(fixTransX, fixTransY);
						fixTrans();
						last.set(curr.x, curr.y);
					}
					break;

				case MotionEvent.ACTION_UP:
					mode = NONE;
					int xDiff = (int) Math.abs(curr.x - start.x);
					int yDiff = (int) Math.abs(curr.y - start.y);
					if (xDiff < CLICK && yDiff < CLICK)
						performClick();
					large = true;
					break;

				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					break;
				}

				setImageMatrix(matrix);
				invalidate();
				return true; // indicate event was handled
			}

		});
	}

	public void setMaxZoom(float x) {
		maxScale = x;
	}

	@SuppressLint("NewApi")
	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mode = ZOOM;
			if (!zoomReported) {
				Log.d(LOG_TAG, "Zoom");
				Utils.reportFlurryEvent("Crop", "Zoom");
				zoomReported = true;
			}
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
			Log.d(LOG_TAG, "saveScale = " + saveScale);
			if (saveScale == 1) {
				saveScale = minScale;
			}
			float origScale = saveScale;
			saveScale *= mScaleFactor;
			Log.d(LOG_TAG, "orig scale = " + origScale);
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				Log.d(LOG_TAG, "saveScale = " + saveScale);
				saveScale = minScale;
				mScaleFactor = minScale / origScale;

				Log.d(LOG_TAG, "mScaleFactor = " + mScaleFactor);
			}
			Log.d(LOG_TAG, "saveScale = " + saveScale);
			if (origWidth * saveScale <= viewWidth
					|| origHeight * saveScale <= viewHeight)
				matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
						viewHeight / 2);
			else
				matrix.postScale(mScaleFactor, mScaleFactor,
						detector.getFocusX(), detector.getFocusY());
			Log.d(LOG_TAG,
					"x=" + detector.getFocusX() + "y=" + detector.getFocusY());

			fixTrans();
			return true;
		}
	}

	void fixTrans() {
		matrix.getValues(m);
		float transX = m[Matrix.MTRANS_X];
		float transY = m[Matrix.MTRANS_Y];

		float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
		float fixTransY = getFixTrans(transY, viewHeight, origHeight
				* saveScale);

		Log.d(LOG_TAG, "transX" + transX);
		Log.d(LOG_TAG, "transY" + transY);

		Log.d(LOG_TAG, "viewWidth" + viewWidth);
		Log.d(LOG_TAG, "viewHeight" + viewHeight);
		Log.d(LOG_TAG, "fixTransX " + fixTransX);
		Log.d(LOG_TAG, "fixTransY " + fixTransY);

		rectLeft = (int) Math.abs(transX / saveScale * initScale);
		rectTop = (int) Math.abs(transY / saveScale * initScale);
		rectWidth = (int) Math.abs(viewWidth / saveScale * initScale);
		rectHeight = (int) Math.abs(viewHeight / saveScale * initScale);
		Log.d(LOG_TAG, "init scale = " + initScale + "transY = " + transY);
		Log.d(LOG_TAG, "rectLeft" + rectLeft);
		Log.d(LOG_TAG, "rectTop" + rectTop);
		Log.d(LOG_TAG, "rectWidth" + rectWidth);
		Log.d(LOG_TAG, "rectHeight" + rectHeight);

		if (fixTransX != 0 || fixTransY != 0)
			matrix.postTranslate(fixTransX, fixTransY);
		// rotate image

	}

	float getFixTrans(float trans, float viewSize, float contentSize) {
		float minTrans, maxTrans;

		if (contentSize <= viewSize) {
			minTrans = 0;
			maxTrans = viewSize - contentSize;
		} else {
			minTrans = viewSize - contentSize;
			maxTrans = 0;
		}

		if (trans < minTrans)
			return -trans + minTrans;
		if (trans > maxTrans)
			return -trans + maxTrans;
		return 0;
	}

	float getFixDragTrans(float delta, float viewSize, float contentSize) {
		Log.d(LOG_TAG, "contentSize = " + contentSize);
		Log.d(LOG_TAG, "viewSize = " + viewSize);
		if (contentSize <= viewSize) {
			return 0;
		}
		return delta;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		viewWidth = desiredWidth;
		viewHeight = desiredHeight;

		setMeasuredDimension(desiredWidth, desiredHeight);
		Log.d(LOG_TAG, "viewWidth = " + viewWidth + " viewHeight = "
				+ viewHeight);
		/*
		 * Log.d(LOG_TAG, "viewWidth: " + viewWidth + " viewHeight : " +
		 * viewHeight); Log.d(LOG_TAG, "desiredWidth: " + desiredWidth +
		 * " desiredHeight : " + desiredHeight);
		 */
		//
		// Rescales image on rotation
		//
		if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
				|| viewWidth == 0 || viewHeight == 0)
			return;
		oldMeasuredHeight = (int) viewHeight;
		oldMeasuredWidth = (int) viewWidth;

		if (saveScale == minScale) {

			/*
			 * Log.d(LOG_TAG, "fit to screen"); // Fit to screen. float scale;
			 * 
			 * Drawable drawable = getDrawable(); if (drawable == null ||
			 * drawable.getIntrinsicWidth() == 0 ||
			 * drawable.getIntrinsicHeight() == 0) return; int bmWidth =
			 * drawable.getIntrinsicWidth(); int bmHeight =
			 * drawable.getIntrinsicHeight();
			 * 
			 * 
			 * Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " +
			 * bmHeight);
			 * 
			 * 
			 * float scaleX = (float) viewWidth / (float) bmWidth; float scaleY
			 * = (float) viewHeight / (float) bmHeight; scale = Math.min(scaleX,
			 * scaleY); matrix.setScale(scale, scale);
			 * 
			 * // Center the image float redundantYSpace = (float) viewHeight -
			 * (scale * (float) bmHeight); float redundantXSpace = (float)
			 * viewWidth - (scale * (float) bmWidth); redundantYSpace /= (float)
			 * 2; redundantXSpace /= (float) 2;
			 * 
			 * matrix.postTranslate(redundantXSpace, redundantYSpace);
			 */
			// origWidth = viewWidth - 2 * redundantXSpace;
			// origHeight = viewHeight - 2 * redundantYSpace;

			saveScale = minScale;
		}
		fixTrans();
	}

	@SuppressLint("NewApi")
	private void loadRect(Canvas canvas) {
		if (rectWidth == 0 || rectHeight == 0) {
			return;
		}
		// check rotation
		if (rotate % 4 == 0) {
			if (rectWidth > maxBitmapWidth) {
				rectWidth = maxBitmapWidth;
			}
			if (rectHeight > maxBitmapHeight) {
				rectHeight = maxBitmapHeight;
			}
		} else {
			if (rectWidth > maxBitmapHeight) {
				rectWidth = maxBitmapHeight;
			}
			if (rectHeight > maxBitmapWidth) {
				rectHeight = maxBitmapWidth;
			}
		}
		
		
		
		float rectRight = rectLeft + rectWidth;
		float rectBottom = rectTop + rectHeight;
		
		// check rotation
		if (rotate % 4 == 0) {
			if (rectRight > maxBitmapWidth) {
				rectRight = maxBitmapWidth;
			}
			if (rectBottom > maxBitmapHeight) {
				rectBottom = maxBitmapHeight;
			}
		} else {
			if (rectRight > maxBitmapHeight) {
				rectRight = maxBitmapHeight;
			}
			if (rectBottom > maxBitmapWidth) {
				rectBottom = maxBitmapWidth;
			}
		}

		Log.d(LOG_TAG, "rectWidth" + rectWidth);
		Log.d(LOG_TAG, "rectHeight" + rectHeight);

		int nLeft = 0;
		int nTop = 0;
		int nRight = 0;
		int nBottom = 0;

		if (rotate == 0) {
			Log.d(LOG_TAG, "test");
			nLeft = (int) rectLeft;
			nTop = (int) rectTop;
			nRight = (int) rectRight;
			nBottom = (int) rectBottom;
			if (mirrorH) {
				nLeft = (int) (maxBitmapWidth - nLeft - rectWidth);
				nRight = (int) (nLeft + rectWidth);
			}
			if (mirrorV) {
				nTop = (int) (maxBitmapHeight - nTop - rectHeight);

				nBottom = (int) (nTop + rectHeight);
			}
		}

		if (rotate == 180) {
			nLeft = (int) (maxBitmapWidth - rectLeft - rectWidth);
			nTop = (int) (maxBitmapHeight - rectTop - rectHeight);
			nRight = (int) (nLeft + rectWidth);
			nBottom = (int) (nTop + rectHeight);
			if (mirrorH) {
				nLeft = (int) rectLeft;
				nRight = (int) rectRight;
			}
			if (mirrorV) {
				nTop = (int) rectTop;
				nBottom = (int) (rectBottom);
			}
		}

		if (rotate == 90) {
			nLeft = (int) rectTop;
			nTop = (int) (maxBitmapHeight - rectLeft - rectWidth);
			if (nTop < 0)
				nTop = 0;
			nRight = (int) rectBottom;
			nBottom = (int) (nTop + rectWidth);
			if (mirrorH) {
				nTop = (int) rectLeft;
				nBottom = (int) (rectBottom);
			}
			if (mirrorV) {
				Log.d(LOG_TAG, "maxBitmapWidth = " + maxBitmapWidth
						+ " nLeft = " + nLeft + " rectHeight = " + rectHeight);
				nLeft = (int) (maxBitmapWidth - nLeft - rectHeight);
				nRight = (int) (nLeft + rectHeight);
			}
		}

		if (rotate == 270) {
			nLeft = (int) (maxBitmapWidth - rectTop - rectHeight);
			nTop = (int) (rectLeft);
			nRight = (int) (nLeft + rectHeight);
			nBottom = (int) (rectRight);
			if (mirrorH) {
				nTop = (int) (maxBitmapHeight - rectLeft - rectWidth);
				nBottom = (int) (nTop + rectWidth);
			}
			if (mirrorV) {
				nLeft = (int) rectTop;
				nRight = (int) rectBottom;
			}
		}

		if (nTop < 0)
			nTop = 0;
		if (nLeft < 0)
			nLeft = 0;
		if (nRight > maxBitmapWidth) {
			nRight = (int) maxBitmapWidth;
		}
		if (nBottom > maxBitmapHeight) {
			nBottom = (int) maxBitmapHeight;
		}

		bounds = new Rect(nLeft, nTop, nRight, nBottom);

		Log.d(LOG_TAG, "rectLeft = " + nLeft + ", rectTop = " + nTop
				+ ", rectRight = " + nRight + " rectBottom = " + nBottom);

		// load reduced bitmap region
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {

			FileInputStream istream = null;
			BitmapRegionDecoder decoder = null;
			try {
				istream = new FileInputStream(new File(input));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				decoder = BitmapRegionDecoder.newInstance(istream, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		if (bounds != null) {
			BitmapFactory.Options optionsZoom = new BitmapFactory.Options();
			int inSampleSize = calculateRegionInSampleSize(nRight - nLeft,
					nBottom - nTop, (int) origWidth, (int) origHeight);
			if (inSampleSize > 1) {
				optionsZoom.inSampleSize = inSampleSize;
			}
			if (bitmap != null) {
				bitmap.recycle();
			}
			System.gc();
			if (decoder != null){
				bitmap = decoder.decodeRegion(bounds, optionsZoom);
				Log.d(LOG_TAG, "bitmap region width = " + bitmap.getWidth()
						+ " region height = " + bitmap.getHeight());
			}
			
		}
		if (bitmap == null){
			return;
		}
		Log.d(LOG_TAG, "draw and mirrorH");
		Matrix matrixMirror = new Matrix();
		matrixMirror.setRotate(rotate);
		matrixMirror.postScale(mirrorH ? -1 : 1, mirrorV ? -1 : 1);
		Bitmap mirrored = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrixMirror, false);
		if (mirrored != null && bitmap != mirrored){
			bitmap = mirrored;
		}
		Log.d(LOG_TAG,
				"after mirror " + bitmap.getWidth() + " " + bitmap.getHeight());
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		float scale = viewHeight / bitmap.getHeight();
		Matrix matrix2 = new Matrix();
		matrix2.postScale(scale, scale);
		canvas.drawBitmap(bitmap, matrix2, paint);
		canvas.restore();
		}
	}

	public static int calculateRegionInSampleSize(int regionWidth,
			int regionHeight, int reqWidth, int reqHeight) {
		final int height = regionHeight;
		final int width = regionWidth;
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

	@SuppressLint("NewApi")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (large) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
				loadRect(canvas);
			}
			large = false;
		}
	}

	public void rotate() {
		rotate += 90;
		if (rotate > 270) {
			rotate = 0;
		}
		// calculate new bitmap ratio
		ratio = 1 / ratio;
		doRotate = true;
		refreshBitmap();
	}

	public void setContainer(Container container, float svRatio) {
		this.container = container;
		this.ratio = svRatio;
	}// /

	public void mirrorV() {
		mirrorV = !mirrorV;
		doMirrorV = true;
		refreshBitmap();
		Log.d(LOG_TAG, "mirrorV click");
	}

	public void mirrorH() {
		mirrorH = !mirrorH;
		doMirrorH = true;
		refreshBitmap();
		Log.d(LOG_TAG, "mirrorH click");
	}

	public void setBitmap(String _input, Container _container, float _ratio) {
		container = _container;
		input = _input;
		ratio = _ratio;
		doRotate = true; // ?
		// Get image size
		BitmapSize size = Utils.getBitmapSizeFromFile(input);
		maxBitmapWidth = size.width;
		maxBitmapHeight = size.height;
		resetScale(); // ?
		refreshBitmap();
	}

	private void resetScale() {
		saveScale = 1;
		this.measure(0, 0);

	}

	private void refreshBitmap() {
		if (doRotate) {
			this.matrix.getValues(m);
			rotateBitmap();
			resetScale();
			fixTrans();
			this.invalidate();
			// focusRotate(oldTransX, oldTransY, oldInitScale);
			doRotate = false;
		}
		if (doMirrorH || doMirrorV) {
			mirrorBitmap();
			focusMirror();
			doMirrorH = false;
			doMirrorV = false;
		}
		invalidate();
		fixTrans();
		large = true;
	}

	private void focusMirror() {
		this.matrix.getValues(m);
		float transX = m[Matrix.MTRANS_X];
		float transY = m[Matrix.MTRANS_Y];
		float maxX = saveScale * thumb.getWidth();
		float maxY = saveScale * thumb.getHeight();
		Log.d(LOG_TAG, "transX, transY " + transX + " , " + transY);
		Log.d(LOG_TAG, "maxX, maxY " + maxX + " , " + maxY);
		float transX2 = 0;
		float transY2 = 0;

		Log.d(LOG_TAG, "saveScale = " + saveScale);
		Log.d(LOG_TAG, "doMirrorV = " + doMirrorV);
		Log.d(LOG_TAG, "thumb.getWidth() = " + thumb.getWidth());
		Log.d(LOG_TAG, "thumb.getHeight() = " + thumb.getHeight());

		if (doMirrorH) {

			if (mirrorH) {
				transX2 = -(maxX - 2 * Math.abs(transX) - viewWidth);
			} else {
				transX2 = 2 * Math.abs(transX) - maxX + viewWidth;
			}
		}
		if (doMirrorV) {

			if (mirrorV) {
				transY2 = -(maxY - 2 * Math.abs(transY) - viewHeight);
			} else {
				transY2 = 2 * Math.abs(transY) - maxY + viewHeight;
			}
		}
		Log.d(LOG_TAG, "mirrorH = " + mirrorH);
		Log.d(LOG_TAG, "transX, transY " + transX + " , " + transY);
		Log.d(LOG_TAG, "transX2, transY2 " + transX2 + " , " + transY2);
		this.matrix.postTranslate(transX2, transY2);
		setImageMatrix(this.matrix);
		invalidate();
	}

	private void rotateBitmap() {
		if (container == null) {
			return;
		}

		mirrorH = false;
		mirrorV = false;
		// get container size
		float containerWidth = container.getWidth();
		float containerHeight = container.getHeight();
		containerRatio = containerWidth / containerHeight;

		// calculate new bitmap size
		float svWidth = containerWidth;
		float svHeight = containerHeight;

		if (ratio < containerRatio) {
			svWidth = (int) (containerHeight * ratio);
		} else if (ratio > containerRatio) {
			svHeight = (int) (containerWidth / ratio);
		}

		BitmapSize size = null;
		size = Utils.getBitmapSizeFromFile(input);

		float btmWidth = 0;
		float btmHeight = 0;
		float w = 0;
		float h = 0;
		if (rotate % 4 == 0) {
			w = svWidth;
			h = svHeight;
		} else {
			w = svHeight;
			h = svWidth;
		}
		Log.d(LOG_TAG, "w = " + w + " h = " + h);
		float bitmapRatio = size.width / size.height;
		float scaleX = size.width / w;
		float scaleY = size.height / h;
		Log.d(LOG_TAG, "scaleX = " + scaleX + " scaleY = " + scaleY);
		if (scaleX < scaleY) {
			Log.d(LOG_TAG, "scaleX < scaleY");
			initScale = scaleX;
			btmWidth = w;
			btmHeight = btmWidth / bitmapRatio;
		} else {
			Log.d(LOG_TAG, "scaleX > scaleY");
			initScale = scaleY;
			btmHeight = h;
			btmWidth = btmHeight * bitmapRatio;
		}

		desiredWidth = (int) svWidth;
		desiredHeight = (int) svHeight;

		if (rotate % 4 == 0) {
			origHeight = btmHeight;
			origWidth = btmWidth;
		} else {
			origHeight = btmWidth;
			origWidth = btmHeight;
		}
		Log.d(LOG_TAG, "svWidth = " + svWidth + " svHeight = " + svHeight);
		Log.d(LOG_TAG, "btmWidth = " + btmWidth + " btmHeight = " + btmHeight);

		Log.d(LOG_TAG, "origWidth = " + origWidth + " origHeight = "
				+ origHeight);

		Log.d(LOG_TAG, " bitmap width = " + btmWidth + " bitmap height = "
				+ btmHeight);
		thumb = Utils.getThumbnailFromPath(input, (int) btmWidth,
				(int) btmHeight);
		if (thumb == null){
			return;
		}
		boolean b = mirrorH;
		mirrorH = mirrorV;
		mirrorV = b;
		Log.d(LOG_TAG, "mirrorH = " + mirrorH + " mirrorV = " + mirrorV);
		matrix.setScale(mirrorH ? -1 : 1, mirrorV ? -1 : 1);
		setImageMatrix(this.matrix);

		Matrix matrix = new Matrix();
		matrix.postRotate(rotate);
		fixTrans();
		Bitmap rotBitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(),
				thumb.getHeight(), matrix, true);
		if (rotBitmap != thumb) {
			thumb.recycle();
			System.gc();
			thumb = rotBitmap;
		}
		this.setImageBitmap(thumb);
		this.invalidate();
	}

	private void mirrorBitmap() {
		if (thumb == null){
			return;
		}
		Matrix matrix = new Matrix();
		if (doMirrorH) {
			matrix.postScale(-1, 1);
		}
		if (doMirrorV) {
			matrix.postScale(1, -1);
		}
		Bitmap mirrored = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(),
				thumb.getHeight(), matrix, true);
		if (mirrored != thumb) {
			thumb.recycle();
			System.gc();
			thumb = mirrored;
		}
		// refresh image view
		this.setImageBitmap(thumb);
		invalidate();
	}

//	private void focusRotate(float oldTransX, float oldTransY,
//			float oldInitScale) {
//		float transX = oldTransY * (initScale / oldInitScale);
//		float transY = oldTransX * (initScale / oldInitScale);
//
//		// this.matrix.postTranslate(transX, transY);
//		Log.d(LOG_TAG, "transX = " + transX + ", transY = " + transY);
//		setImageMatrix(this.matrix);
//		invalidate();
//	}

	public int getRotate() {
		return rotate;
	}

	public boolean getMirrorH() {
		return mirrorH;
	}

	public boolean getMirrorV() {
		return mirrorV;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		doRotate = true;
		refreshBitmap();
	}

	public void rotateOnlyBitmap() {
		fixTrans();
		saveScale = 1;
		rotate+= 90;
		if (rotate > 270){
			rotate = 0;
		}
		
		if (container == null) {
			return;
		}

		mirrorH = false;
		mirrorV = false;
		// get container size
		float containerWidth = container.getWidth();
		float containerHeight = container.getHeight();
		containerRatio = containerWidth / containerHeight;

		// calculate new bitmap size
		float svWidth = containerWidth;
		float svHeight = containerHeight;

		if (ratio < containerRatio) {
			svWidth = (int) (containerHeight * ratio);
		} else if (ratio > containerRatio) {
			svHeight = (int) (containerWidth / ratio);
		}

		BitmapSize size = null;
		size = Utils.getBitmapSizeFromFile(input);

		float btmWidth = 0;
		float btmHeight = 0;
		float w = 0;
		float h = 0;
		if (rotate % 4 == 0) {
			w = svWidth;
			h = svHeight;
		} else {
			w = svHeight;
			h = svWidth;
		}
		Log.d(LOG_TAG, "w = " + w + " h = " + h);
		float bitmapRatio = size.width / size.height;
		float scaleX = size.width / w;
		float scaleY = size.height / h;
		Log.d(LOG_TAG, "scaleX = " + scaleX + " scaleY = " + scaleY);
		if (scaleX < scaleY) {
			Log.d(LOG_TAG, "scaleX < scaleY");
			initScale = scaleX;
			btmWidth = w;
			btmHeight = btmWidth / bitmapRatio;
		} else {
			Log.d(LOG_TAG, "scaleX > scaleY");
			initScale = scaleY;
			btmHeight = h;
			btmWidth = btmHeight * bitmapRatio;
		}

		desiredWidth = (int) svWidth;
		desiredHeight = (int) svHeight;

		if (rotate % 4 == 0) {
			origHeight = btmHeight;
			origWidth = btmWidth;
		} else {
			origHeight = btmWidth;
			origWidth = btmHeight;
		}
		Log.d(LOG_TAG, "svWidth = " + svWidth + " svHeight = " + svHeight);
		Log.d(LOG_TAG, "btmWidth = " + btmWidth + " btmHeight = " + btmHeight);

		Log.d(LOG_TAG, "origWidth = " + origWidth + " origHeight = "
				+ origHeight);

		Log.d(LOG_TAG, " bitmap width = " + btmWidth + " bitmap height = "
				+ btmHeight);
		thumb = Utils.getThumbnailFromPath(input, (int) btmWidth,
				(int) btmHeight);

		boolean b = mirrorH;
		mirrorH = mirrorV;
		mirrorV = b;
		Log.d(LOG_TAG, "mirrorH = " + mirrorH + " mirrorV = " + mirrorV);
		matrix.setScale(mirrorH ? -1 : 1, mirrorV ? -1 : 1);
		setImageMatrix(this.matrix);

		Matrix matrix = new Matrix();
		matrix.postRotate(rotate);
		fixTrans();
		Bitmap rotBitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(),
				thumb.getHeight(), matrix, true);
		if (rotBitmap != thumb) {
			thumb.recycle();
			System.gc();
			thumb = rotBitmap;
		}
		fixTrans();
		this.setImageBitmap(thumb);
		this.invalidate();
		
	}

	public void release() {
		this.setImageBitmap(null);
		if (bitmap != null){
			bitmap.recycle();
			System.gc();
		}
		if (thumb != null){
			thumb.recycle();
			System.gc();
		}
	}

}