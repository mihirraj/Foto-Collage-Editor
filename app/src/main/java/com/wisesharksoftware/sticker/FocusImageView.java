package com.wisesharksoftware.sticker;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import it.sephiroth.android.library.imagezoom.graphics.IBitmapDrawable;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

import com.aviary.android.feather.library.graphics.RectD;
import com.aviary.android.feather.library.utils.UIConfiguration;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.sticker.RotationGestureDetector.OnRotationGestureListener;

public class FocusImageView extends ImageViewTouch implements
		OnRotationGestureListener {

	public interface OnHighlightSingleTapUpConfirmedListener {
		void onSingleTapUpConfirmed();
	}

	public static final int GROW = 0;
	public static final int SHRINK = 1;
	private int mMotionEdge = FocusHighlightView.NONE;
	private FocusHighlightView mHighlightView;
	private OnHighlightSingleTapUpConfirmedListener mHighlightSingleTapUpListener;
	private FocusHighlightView mMotionHighlightView;
	private int mCropMinSize = 100;
	private ScaleGestureDetector mFocusScaleDetector;
	private RotationGestureDetector mRotationDetector;
	// private Bitmap localcopyofbitmap;
	// private Bitmap blurredbitmap;
	protected Handler mHandler = new Handler();
	public boolean needToRedraw = true;
	protected double mAspectRatio = 0;
	private boolean mAspectRatioFixed;
	private double currAngle = 0;
	private double prevAngleDiv = 0;
	private double angle = 0;
	private Bitmap blurBitmap;
	private Bitmap bitmapForBlur;

	public FocusImageView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.aviaryCropImageViewStyle);
		mFocusScaleDetector = new ScaleGestureDetector(context,
				new FocusScaleListener());
		mRotationDetector = new RotationGestureDetector(this);
	}

	public FocusImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setBitmapForBlur(Bitmap bitmapForBlur) {
		this.bitmapForBlur = bitmapForBlur;
	}

	public void setOnHighlightSingleTapUpConfirmedListener(
			OnHighlightSingleTapUpConfirmedListener listener) {
		mHighlightSingleTapUpListener = listener;
	}

	@Deprecated
	public void setMinCropSize(int value) {
		mCropMinSize = value;
		if (mHighlightView != null) {
			mHighlightView.setMinSize(value);
		}
	}

	public void setBlurBitmap(Bitmap b) {
		blurBitmap = b;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void init(Context context, AttributeSet attrs, int defStyle) {
		super.init(context, attrs, defStyle);
		mGestureDetector = null;
		mGestureListener = null;
		mScaleListener = null;
		mRotationDetector = null;
		mGestureDetector = new GestureDetector(getContext(),
				new CropGestureListener(), null, true);
		mGestureDetector.setIsLongpressEnabled(false);

		Theme theme = context.getTheme();

		TypedArray array = theme.obtainStyledAttributes(attrs,
				R.styleable.AviaryCropImageView, defStyle, 0);
		mCropMinSize = array.getDimensionPixelSize(
				R.styleable.AviaryCropImageView_aviary_minCropSize, 50);

		array.recycle();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable, Matrix initial_matrix,
			float min_zoom, float max_zoom) {
		mMotionHighlightView = null;
		super.setImageDrawable(drawable, initial_matrix, min_zoom, max_zoom);
	}

	@Override
	protected void onLayoutChanged(int left, int top, int right, int bottom) {
		super.onLayoutChanged(left, top, right, bottom);
		mHandler.post(onLayoutRunnable);
	}

	Runnable onLayoutRunnable = new Runnable() {

		@Override
		public void run() {
			try{
			final Drawable drawable = getDrawable();

			if (drawable != null
					&& ((IBitmapDrawable) drawable).getBitmap() != null) {
				if (mHighlightView != null) {
					if (mHighlightView.isRunning()) {
						mHandler.post(this);
					} else {
						Log.d(LOG_TAG, "onLayoutRunnable.. running");
						mHighlightView.getMatrix().set(getImageMatrix());
						mHighlightView.invalidate();
					}
				}
			}
			}catch(Exception e){
				new ExceptionHandler(e,"FocusImageView onLayoutRunnable");
			}
		}
	};

	@Override
	protected void postTranslate(float deltaX, float deltaY) {
		super.postTranslate(deltaX, deltaY);

		if (mHighlightView != null) {

			if (mHighlightView.isRunning()) {
				return;
			}

			if (getScale() != 1) {
				float[] mvalues = new float[9];
				getImageMatrix().getValues(mvalues);
				final float scale = mvalues[Matrix.MSCALE_X];
				mHighlightView.getCropRectD().offset(-deltaX / scale,
						-deltaY / scale);
			}

			mHighlightView.getMatrix().set(getImageMatrix());
			mHighlightView.invalidate();
		}
	}

	private Rect mRect1 = new Rect();
	private Rect mRect2 = new Rect();

	@Override
	protected void postScale(float scale, float centerX, float centerY) {
		if (mHighlightView != null) {

			if (mHighlightView.isRunning())
				return;

			RectD cropRect = mHighlightView.getCropRectD();
			mHighlightView.getDisplayRect(getImageViewMatrix(),
					mHighlightView.getCropRectD(), mRect1);

			super.postScale(scale, centerX, centerY);

			mHighlightView.getDisplayRect(getImageViewMatrix(),
					mHighlightView.getCropRectD(), mRect2);

			float[] mvalues = new float[9];
			getImageViewMatrix().getValues(mvalues);
			final float currentScale = mvalues[Matrix.MSCALE_X];

			cropRect.offset((mRect1.left - mRect2.left) / currentScale,
					(mRect1.top - mRect2.top) / currentScale);
			cropRect.right += -(mRect2.width() - mRect1.width()) / currentScale;
			cropRect.bottom += -(mRect2.height() - mRect1.height())
					/ currentScale;

			mHighlightView.getMatrix().set(getImageMatrix());
			mHighlightView.getCropRectD().set(cropRect);
			mHighlightView.invalidate();
		} else {
			super.postScale(scale, centerX, centerY);
		}
	}

	private boolean ensureVisible(FocusHighlightView hv) {
		Rect r = hv.getDrawRect();
		int panDeltaX1 = Math.max(0, getLeft() - r.left);
		int panDeltaX2 = Math.min(0, getRight() - r.right);
		int panDeltaY1 = Math.max(0, getTop() - r.top);
		int panDeltaY2 = Math.min(0, getBottom() - r.bottom);
		int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
		int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

		if (panDeltaX != 0 || panDeltaY != 0) {
			panBy(panDeltaX, panDeltaY);
			return true;
		}
		return false;
	}

	// public void saveBitmap(Bitmap b) {
	// localcopyofbitmap = b;
	// }

	public Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
		double max_coeff = -999;
		double min_coeff = 999;

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				// int x_center = w / 2;
				// int y_center = h / 2;
				// int point_radius = 250;
				// int point_radius2 = point_radius * point_radius;
				// int radiusRing = 50;
				// int radiusRing2 = radiusRing * radiusRing;
				// int curr_radius2 = (x_center - x) * (x_center - x) +
				// (y_center - y) * (y_center - y);
				// //if (curr_radius2 > point_radius2) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				/*
				 * } else if (curr_radius2 > (point_radius - radiusRing) *
				 * (point_radius - radiusRing)) { double interval =
				 * Math.sqrt(curr_radius2) - (point_radius - radiusRing); double
				 * interval2 = interval * interval; double coeff = 1 - (interval
				 * / (radiusRing * 1.0)); if (coeff > max_coeff) { max_coeff =
				 * coeff; } if (coeff < min_coeff) { min_coeff = coeff; }
				 * 
				 * int tempr = (pix[yi] & 0xff0000) >> 16; int tempg = (pix[yi]
				 * & 0x00ff00) >> 8; int tempb = (pix[yi] & 0x0000ff);
				 * 
				 * pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | (
				 * dv[gsum] << 8 ) | dv[bsum]; int pixr = (pix[yi] & 0xff0000)
				 * >> 16; int pixg = (pix[yi] & 0x00ff00) >> 8; int pixb =
				 * (pix[yi] & 0x0000ff);
				 * 
				 * tempr = (int) (tempr * coeff + pixr * (1 - coeff)); tempg =
				 * (int) (tempg * coeff + pixg * (1 - coeff)); tempb = (int)
				 * (tempb * coeff + pixb * (1 - coeff));
				 * 
				 * if (tempr < 0) { tempr = 0; }
				 * 
				 * if (tempg < 0) { tempg = 0; }
				 * 
				 * if (tempb < 0) { tempb = 0; } if (tempr > 255) { tempr = 255;
				 * } if (tempg > 255) { tempg = 255; } if (tempb > 255) { tempb
				 * = 255; }
				 * 
				 * pix[yi] = ( 0xff000000 & pix[yi] ) | ( tempr << 16 ) | (
				 * tempg << 8 ) | tempb; }
				 */
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		// Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		//Log.d("AAA", "min_coeff = " + min_coeff + " max_coeff = " + max_coeff);
		return (bitmap);
	}

	// class BlurAsyncTask extends AsyncTask<Rect, Integer, Boolean> {
	//
	// @Override
	// protected Boolean doInBackground(Rect... params) {
	// blurredbitmap = fastblur(localcopyofbitmap, params[0], 15);
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Boolean result) {
	// needToRedraw = false;
	// if (FocusImageView.this != null) {
	// FocusImageView.this.invalidate();
	// }
	// super.onPostExecute(result);
	// }
	//
	// }

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		if (mHighlightView != null) {
			if (mHighlightView.getBlurBitmap() == null
					|| mHighlightView.getRealBlurBitmap().isRecycled()) {
				if (blurBitmap.isRecycled() &&  !bitmapForBlur.isRecycled() && bitmapForBlur != null) {
					blurBitmap = fastblur(bitmapForBlur, 5);
				}
				Log.d("IsRecycle", blurBitmap.isRecycled() + "");
				mHighlightView.setBlurBitmap(null);
				mHighlightView.setRealBlurBitmap(blurBitmap);
			}
			mHighlightView.draw(canvas);
		}

		// -----------------------------------------------------------------------------------------------------------
		// Drawable drawable = getDrawable();
		// if (drawable != null) {
		//
		// int top = (int) getBitmapRect().top;
		// int bottom = (int) getBitmapRect().bottom;
		// int left = (int) getBitmapRect().left;
		// int right = (int) getBitmapRect().right;
		// //Log.d("animate", "top = " + top);
		// //drawable.setBounds(left, top, right, bottom);
		// Paint paint = new Paint();
		// if (localcopyofbitmap != null) {
		// paint.setColor(0xFFffffff);
		// paint.setStyle(Paint.Style.FILL);
		// if (needToRedraw) {
		// if (getCropRect() != null) {
		// new BlurAsyncTask().execute(getCropRect());
		// }
		// }
		// if (blurredbitmap != null) {
		// canvas.drawBitmap(blurredbitmap, left, top, paint);
		// } else {
		// canvas.drawBitmap(localcopyofbitmap, left, top, paint);
		// }
		// //mBitmap = fastblur(mBitmap, 5);
		// }
		//
		// //drawable.draw(canvas);
		// if (mHighlightView != null) {
		// mHighlightView.draw(canvas);
		// }
		// }
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (null != mHighlightView) {
			mHighlightView.onSizeChanged(this, w, h, oldw, oldh);
		}
	}

	public void setHighlightView(FocusHighlightView hv) {
		if (mHighlightView != null) {
			mHighlightView.dispose();
		}

		mMotionHighlightView = null;
		mHighlightView = hv;
		invalidate();
	}

	public FocusHighlightView getHighlightView() {
		return mHighlightView;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		mFocusScaleDetector.onTouchEvent(event);
		mRotationDetector.onTouchEvent(event);
		int action = event.getAction() & MotionEvent.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_UP: {

			if (mHighlightView != null) {
				mHighlightView.setMode(FocusHighlightView.Mode.None);
				// /////////
				float width = getDrawable().getIntrinsicWidth();
				float height = getDrawable().getIntrinsicHeight();
				RectD imageRect = new RectD(0, 0, (int) width, (int) height);

				Matrix mImageMatrix = getImageMatrix();
				RectD cropRect = computeFinalCropRect(1);
				mHighlightView.animateAppear(this, mImageMatrix, imageRect,
						cropRect, FocusHighlightView.APPEAR_MODE_UP);

				// /////////
				needToRedraw = true;
				postInvalidate();
			}

			mMotionHighlightView = null;
			mMotionEdge = FocusHighlightView.NONE;
			break;
		}
		case MotionEvent.ACTION_DOWN: {

			if (mHighlightView != null) {
				mHighlightView.setMode(FocusHighlightView.Mode.None);
				// /////////
				float width = getDrawable().getIntrinsicWidth();
				float height = getDrawable().getIntrinsicHeight();
				RectD imageRect = new RectD(0, 0, (int) width, (int) height);

				Matrix mImageMatrix = getImageMatrix();
				RectD cropRect = computeFinalCropRect(1);
				mHighlightView.animateAppear(this, mImageMatrix, imageRect,
						cropRect, FocusHighlightView.APPEAR_MODE_DOWN);

				// /////////
				postInvalidate();
			}

			// mMotionHighlightView = null;
			// mMotionEdge = HighlightView.NONE;
			break;
		}

		}
		return true;
	}

	class CropGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			mMotionHighlightView = null;
			FocusHighlightView hv = mHighlightView;
			Log.d("animate", "OnDown");

			if (hv != null) {
				Log.d("animate", "hv != null");

				int edge = FocusHighlightView.MOVE;// hv.getHit( e.getX(),
													// e.getY() );
				if (edge != FocusHighlightView.NONE) {
					mMotionEdge = edge;
					mMotionHighlightView = hv;
					mMotionHighlightView
							.setMode((edge == FocusHighlightView.MOVE) ? FocusHighlightView.Mode.Move
									: FocusHighlightView.Mode.Grow);

					postInvalidate();
				}
			}
			return super.onDown(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			mMotionHighlightView = null;

			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mMotionHighlightView = null;

			if (mHighlightView != null
					&& mMotionEdge == FocusHighlightView.MOVE) {

				if (mHighlightSingleTapUpListener != null) {
					mHighlightSingleTapUpListener.onSingleTapUpConfirmed();
				}
			}
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mDoubleTapEnabled) {
				mMotionHighlightView = null;

				float scale = getScale();
				float targetScale = scale;
				targetScale = FocusImageView.this.onDoubleTapPost(scale,
						getMaxScale());
				targetScale = Math.min(getMaxScale(), Math.max(targetScale, 1));
				zoomTo(targetScale, e.getX(), e.getY(), 200);
				invalidate();
			}
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (e1 == null || e2 == null)
				return false;
			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;

			if (mMotionHighlightView != null
					&& mMotionEdge != FocusHighlightView.NONE) {
				mMotionHighlightView.handleMotion(mMotionEdge, -distanceX,
						-distanceY);

				if (mMotionEdge == FocusHighlightView.MOVE) {
					invalidate(mMotionHighlightView.getInvalidateRect());
				} else {
					postInvalidate();
				}

				ensureVisible(mMotionHighlightView);
				return true;
			} else {
				scrollBy(-distanceX, -distanceY);
				invalidate();
				return true;
			}
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;
			if (mMotionHighlightView != null)
				return false;

			float diffX = e2.getX() - e1.getX();
			float diffY = e2.getY() - e1.getY();

			if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
				scrollBy(diffX / 2, diffY / 2, 300);
				invalidate();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	class FocusScaleListener extends SimpleOnScaleGestureListener {

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			super.onScaleEnd(detector);
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.d("animate", "onScale FocusImage");
			float span = detector.getCurrentSpan() - detector.getPreviousSpan();
			if (span != 0) {
				mMotionHighlightView = null;
				FocusHighlightView hv = mHighlightView;

				if (hv != null) {

					mMotionHighlightView = hv;
					mMotionHighlightView.setMode(FocusHighlightView.Mode.Grow);
					mMotionHighlightView.growBy(span, span, true);

					postInvalidate();
				}
			}
			return true;
		}
	}

	public void setImageBitmap(Bitmap bitmap, double aspectRatio,
			boolean isFixed) {
		mAspectRatio = aspectRatio;
		mAspectRatioFixed = isFixed;
		setImageBitmap(bitmap, null, ImageViewTouchBase.ZOOM_INVALID,
				UIConfiguration.IMAGE_VIEW_MAX_ZOOM);
	}

	public void setAspectRatio(double value, boolean isFixed) {

		if (getDrawable() != null) {
			mAspectRatio = value;
			mAspectRatioFixed = isFixed;
			updateCropView(false);
		}
	}

	@Override
	protected void onDrawableChanged(Drawable drawable) {
		super.onDrawableChanged(drawable);

		if (null != getHandler()) {
			getHandler().post(new Runnable() {

				@Override
				public void run() {
					updateCropView(true);
				}
			});
		}
	}

	public void updateCropView(boolean bitmapChanged) {

		if (bitmapChanged) {
			setHighlightView(null);
		}

		if (getDrawable() == null) {
			setHighlightView(null);
			invalidate();
			return;
		}

		if (getHighlightView() != null) {
			updateAspectRatio(mAspectRatio, getHighlightView(), true, false);
		} else {
			FocusHighlightView hv = new FocusHighlightView(this,
					R.style.AviaryGraphics_CropHighlightView);
			hv.setMinSize(mCropMinSize);
			updateAspectRatio(mAspectRatio, hv, false, true);

			setHighlightView(hv);
		}
		invalidate();
	}

	private void updateAspectRatio(double aspectRatio, FocusHighlightView hv,
			boolean animate, boolean appear) {
		Log.d(LOG_TAG, "updateAspectRatio: " + aspectRatio);

		float width = getDrawable().getIntrinsicWidth();
		float height = getDrawable().getIntrinsicHeight();
		RectD imageRect = new RectD(0, 0, (int) width, (int) height);
		RectF imageBitmapRect = getBitmapRect();

		Matrix mImageMatrix = getImageMatrix();
		RectD cropRect = computeFinalCropRect(aspectRatio);

		if (appear) {
			hv.setup(mImageMatrix, imageRect, imageBitmapRect, cropRect);
			hv.animateAppear(this, mImageMatrix, imageRect, cropRect,
					FocusHighlightView.APPEAR_MODE_FIRST);
			// postInvalidate();
		} else if (animate) {
			// hv.animateTo( this, mImageMatrix, imageRect, cropRect,
			// mAspectRatioFixed );
		} else {
			// hv.setup(mImageMatrix, imageRect, imageBitmapRect, cropRect);
			postInvalidate();
		}
	}

	public void onConfigurationChanged(Configuration config) {
		// Log.d( LOG_TAG, "onConfigurationChanged" );
		if (null != getHandler()) {
			getHandler().postDelayed(new Runnable() {

				@Override
				public void run() {
					setAspectRatio(mAspectRatio, getAspectRatioIsFixed());
				}
			}, 500);
		}
		postInvalidate();
	}

	private RectD computeFinalCropRect(double aspectRatio) {

		float scale = getScale();

		float width = getDrawable().getIntrinsicWidth();
		float height = getDrawable().getIntrinsicHeight();

		RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
		RectF bitmapRect = getBitmapRect();

		RectF rect = new RectF(Math.max(viewRect.left, bitmapRect.left),
				Math.max(viewRect.top, bitmapRect.top), Math.min(
						viewRect.right, bitmapRect.right), Math.min(
						viewRect.bottom, bitmapRect.bottom));

		double cropWidth = Math.min(Math.min(width / scale, rect.width()),
				Math.min(height / scale, rect.height())) * 0.8f;
		double cropHeight = cropWidth;

		if (aspectRatio != 0) {
			if (aspectRatio > 1) {
				cropHeight = cropHeight / (double) aspectRatio;
			} else {
				cropWidth = cropWidth * (double) aspectRatio;
			}
		}

		Matrix mImageMatrix = getImageMatrix();
		Matrix tmpMatrix = new Matrix();

		if (!mImageMatrix.invert(tmpMatrix)) {
			Log.e(LOG_TAG, "cannot invert matrix");
		}

		tmpMatrix.mapRect(viewRect);

		double x = viewRect.centerX() - cropWidth / 2;
		double y = viewRect.centerY() - cropHeight / 2;
		RectD cropRect = new RectD(x, y, (x + cropWidth), (y + cropHeight));

		return cropRect;
	}

	public double getAspectRatio() {
		return mAspectRatio;
	}

	public boolean getAspectRatioIsFixed() {
		return mAspectRatioFixed;
	}

	public Rect getCropRect() {
		if (mHighlightView == null) {
			return null;
		}
		return mHighlightView.getCropRect();
	}

	@Override
	public void OnRotation(RotationGestureDetector rotationDetector) {
		// float angle = rotationDetector.getAngle() / 4;
		prevAngleDiv = angle;
		// angle = ((rotationDetector.getAngle() / 180.0) * 3.14);
		angle = rotationDetector.getAngle();
		// currAngle = currAngle + (prevAngleDiv - angle) / 2;

		// currAngle = currAngle - ((angle + prevAngleDiv) / 2);
		// currAngle = (2 * currAngle - 3 * angle) / 5;
		// currAngle = currAngle - (3 * angle + 2 * prevAngleDiv) / 5;
		currAngle -= angle / 4;
		currAngle = currAngle % 360;
		if (currAngle < -180.f)
			currAngle += 360.0f;
		if (currAngle > 180.f)
			currAngle -= 360.0f;

		Log.d("RotationGestureDetector", "Rotation: " + Double.toString(angle)
				+ " Angle = " + currAngle);
		mMotionHighlightView = null;
		FocusHighlightView hv = mHighlightView;

		if (hv != null) {

			mMotionHighlightView = hv;
			mMotionHighlightView.setMode(FocusHighlightView.Mode.None);
			mMotionHighlightView.setAngle(currAngle / 180.0 * 3.14);

			postInvalidate();
		}
	}
}