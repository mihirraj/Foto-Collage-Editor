package com.wisesharksoftware.sticker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import it.sephiroth.android.library.imagezoom.easing.Easing;
import it.sephiroth.android.library.imagezoom.easing.Quad;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.aviary.android.feather.library.graphics.RectD;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;

public class FocusHighlightView {
	public static final int FOCUS_TYPE_CIRCLE = 0;
	public static final int FOCUS_TYPE_RECT = 1;

	public static final int NONE = 1 << 0;
	public static final int MOVE = 1 << 5;

	public static final int APPEAR_MODE_FIRST = 0;
	public static final int APPEAR_MODE_UP = 1;
	public static final int APPEAR_MODE_DOWN = 2;

	private boolean mHidden;

	private int mParentWidth, mParentHeight;

	private static Handler mHandler = new Handler();

	public enum Mode {
		None, Move, Grow
	}

	private int mMinSize = 0;
	private Mode mMode;
	private Rect mDrawRect = new Rect();
	private RectD mImageRect;
	private RectD mFocusRect;
	private Matrix mMatrix;

	private Paint mOutlinePaint = new Paint();
	private Paint mEreasePaint = new Paint();
	private final Paint mOutlineFill = new Paint();
	private Paint blackPaint = new Paint();
	private int mOutsideFillColor;
	private Paint erasePaint = new Paint();

	private int dWidth, dHeight;
	private RectF imageBitmapRect;
	private int[] colors = new int[3];
	private float[] positions = new float[3];
	private float thickness = 50;
	private boolean stopAppear = false;

	private Bitmap blurBitmap;
	private Bitmap realBlurBitmap;
	private Canvas ereasCanvas;

	private Queue<Runnable> animationQueue;
	private boolean downInProgress = false;

	public FocusHighlightView(View context, int styleId) {
		mOutsideFillColor = Color.WHITE;
		animationQueue = new LinkedList<Runnable>();
		// calculate the initial drawing rectangle
		context.getDrawingRect(mViewDrawingRect);
		mParentWidth = context.getWidth();
		mParentHeight = context.getHeight();
		;
	}

	public void dispose() {
	}

	public void setMinSize(int value) {
		mMinSize = value;
	}

	public void setHidden(boolean hidden) {
		mHidden = hidden;
	}

	public Bitmap getRealBlurBitmap() {
		return realBlurBitmap;
	}

	public void setBlurBitmap(Bitmap blurBitmap) {
		this.blurBitmap = blurBitmap;
	}

	public void setRealBlurBitmap(Bitmap blurBitmap) {
		realBlurBitmap = blurBitmap;/*
									 * realBlurBitmap.createScaledBitmap(blurBitmap
									 * , (int) imageBitmapRect.width(), (int)
									 * imageBitmapRect.height(), false);
									 */
		// this.blurBitmap =
		// realBlurBitmap.createBitmap(realBlurBitmap.getWidth(),
		// realBlurBitmap.getHeight(), Config.ARGB_8888);
		ereasCanvas = new Canvas();
		// ereasCanvas.setBitmap(this.blurBitmap); // drawXY will result on that
		// Bitmap
		// ereasCanvas.drawBitmap(blurBitmap, 0, 0, null);
	}

	public Bitmap getBlurBitmap() {
		return blurBitmap;
	}

	private Rect mViewDrawingRect = new Rect();

	private Path mPath = new Path();
	private Path mInversePath = new Path();
	private Rect tmpRect4 = new Rect();

	private RectF tmpDrawRect2F = new RectF();
	private RectF tmpDrawRectF = new RectF();
	private RectF tmpDisplayRectF = new RectF();
	private Rect tmpRectMotion = new Rect();

	protected volatile boolean mRunning = false;
	protected int animationDurationMs = 500;
	protected int animationAppearDurationMs = 1000;

	protected Easing mEasing = new Quad();
	private double mAngle = 0;

	private int focusType = FOCUS_TYPE_CIRCLE;

	public void draw(Canvas canvas) {
		if (mHidden)
			return;

		/*
		 * if (mImageRect.width() < mParentWidth) { imageBitmapRect.left =
		 * (float) ((mParentWidth - mImageRect.width()) / 2);
		 * imageBitmapRect.right = mParentWidth - imageBitmapRect.left; }
		 */
		Log.d("BLUR", realBlurBitmap + "");

		if (focusType == FOCUS_TYPE_CIRCLE) {
			if (ereasCanvas != null) {

				/*
				 * if (blurBitmap != null) { new
				 * RecycleBitmap(blurBitmap).start(); }
				 */
				if (blurBitmap == null || blurBitmap.isRecycled()) {
					blurBitmap = Bitmap.createBitmap(mParentWidth,
							mParentHeight, Config.ARGB_8888);

					ereasCanvas.setBitmap(blurBitmap); // drawXY will result on
				}
				if (!realBlurBitmap.isRecycled())
					ereasCanvas.drawBitmap(realBlurBitmap, null, new Rect(
							(int) imageBitmapRect.left,
							(int) imageBitmapRect.top,
							(int) imageBitmapRect.right,
							(int) imageBitmapRect.bottom), null);
			}
			mPath.reset();

			mInversePath.reset();

			erasePaint
					.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

			tmpDrawRectF.set(mDrawRect);
			tmpDrawRect2F.set(mViewDrawingRect);

			mInversePath.addRect(new RectF(imageBitmapRect.left,
					imageBitmapRect.top, imageBitmapRect.right,
					imageBitmapRect.bottom - 1), Path.Direction.CCW);
			mInversePath.addCircle(
					(tmpDrawRectF.left + tmpDrawRectF.right) / 2,
					(tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
					(tmpDrawRectF.right - tmpDrawRectF.left) / 2,
					Path.Direction.CW);
			tmpDrawRectF.set(mDrawRect);
			mPath.addCircle((tmpDrawRectF.left + tmpDrawRectF.right) / 2,
					(tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
					(tmpDrawRectF.right - tmpDrawRectF.left) / 2,
					Path.Direction.CW);
			if (ereasCanvas != null) {
				// ereasCanvas.drawColor(Color.BLACK);
				ereasCanvas.drawPath(mInversePath, mOutlineFill);// outline rect
																	// fill
			}
			float x = (tmpDrawRectF.left + tmpDrawRectF.right) / 2;
			float y = (tmpDrawRectF.top + tmpDrawRectF.bottom) / 2;
			float radius = (tmpDrawRectF.right - tmpDrawRectF.left) / 2 + 1;

			float draw_thickness = thickness;
			if (draw_thickness > radius) {
				draw_thickness = radius;
			}

			positions[0] = 0;
			positions[1] = (radius - draw_thickness) / radius;
			positions[2] = 1;

			colors[0] = Color.argb(0, 0, 0, 0);
			colors[1] = Color.argb(0, 0, 0, 0);
			colors[2] = Color.argb(153, 255, 255, 255);
			mOutlinePaint.setShader(new RadialGradient(x, y, radius, colors,
					positions, TileMode.CLAMP));
			mOutlinePaint.setAntiAlias(false);
			mOutlinePaint.setDither(true);
			MaskFilter mBlur = new BlurMaskFilter(draw_thickness,
					BlurMaskFilter.Blur.NORMAL);
			erasePaint
					.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

			erasePaint.setMaskFilter(mBlur);
			Path erasePath = new Path();
			/*
			 * erasePath.addRect(new RectF(tmpDrawRectF.left, tmpDrawRectF.top,
			 * tmpDrawRectF.right, tmpDrawRectF.bottom), Path.Direction.CW);
			 */
			erasePath.addCircle((tmpDrawRectF.left + tmpDrawRectF.right) / 2,
					(tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
					(tmpDrawRectF.right - tmpDrawRectF.left) / 2
							- draw_thickness, Path.Direction.CW);

			if (ereasCanvas != null) {

				ereasCanvas.drawPath( /* mPath */erasePath, erasePaint);// erase
				ereasCanvas.drawPath(mPath, mOutlinePaint);

			} // circle

			// mOutlinePaint.setColor(Color.argb(0, 0, 0, 0));

			// float radiusAngle = (tmpDrawRectF.right - tmpDrawRectF.left) / 2;
			// canvas.drawCircle((float) ((tmpDrawRectF.left +
			// tmpDrawRectF.right) / 2 + radiusAngle/* * Math.cos(mAngle)*/),
			// (float) ((tmpDrawRectF.top + tmpDrawRectF.bottom) / 2/* +
			// radiusAngle * Math.sin(mAngle)*/), 50, blackPaint);
		} else if (focusType == FOCUS_TYPE_RECT) {
			mPath.reset();

			mInversePath.reset();

			tmpDrawRectF.set(mDrawRect);
			tmpDrawRect2F.set(mViewDrawingRect);

			// mInversePath.addCircle(
			// (tmpDrawRectF.left + tmpDrawRectF.right) / 2,
			// (tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
			// (tmpDrawRectF.right - tmpDrawRectF.left) / 2,
			// Path.Direction.CCW);
			// mInversePath.addRect(imageBitmapRect, Path.Direction.CW);
			//
			// tmpDrawRectF.set(mDrawRect);
			// mPath.addCircle((tmpDrawRectF.left + tmpDrawRectF.right) / 2,
			// (tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
			// (tmpDrawRectF.right - tmpDrawRectF.left) / 2,
			// Path.Direction.CW);
			//
			// canvas.drawPath(mInversePath, mOutlineFill);// outline rect fill

			float x = (tmpDrawRectF.left + tmpDrawRectF.right) / 2;
			float y = (tmpDrawRectF.top + tmpDrawRectF.bottom) / 2;
			float radius = (tmpDrawRectF.right - tmpDrawRectF.left) / 2 + 1;

			float draw_thickness = thickness;
			if (draw_thickness > radius) {
				draw_thickness = radius;
			}
			MaskFilter mBlur = new BlurMaskFilter(draw_thickness,
					BlurMaskFilter.Blur.NORMAL);
			//
			// positions[0] = 0;
			// positions[1] = (radius - draw_thickness) / radius;
			// positions[2] = 1;
			//
			// colors[0] = Color.argb(0, 0, 0, 0);
			// colors[1] = Color.argb(0, 0, 0, 0);
			// colors[2] = Color.argb(153, 255, 255, 255);
			// mOutlinePaint.setShader(new RadialGradient(x, y, radius, colors,
			// positions, TileMode.CLAMP));
			mOutlinePaint.setAntiAlias(false);
			mOutlinePaint.setDither(true);

			// erasePaint
			// .setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			// erasePaint.setMaskFilter(mBlur);
			// Path erasePath = new Path();
			// erasePath.addCircle((tmpDrawRectF.left + tmpDrawRectF.right) / 2,
			// (tmpDrawRectF.top + tmpDrawRectF.bottom) / 2,
			// (tmpDrawRectF.right - tmpDrawRectF.left) / 2
			// - draw_thickness, Path.Direction.CW);

			// canvas.drawPath( /* mPath */erasePath, erasePaint);// erase
			// circle
			float width2 = (imageBitmapRect.right - imageBitmapRect.left) / 2;
			canvas.drawLine(x - width2, (float) ((x - width2) + radius), x
					+ width2, (float) ((x + width2) + radius), mOutlinePaint);
			canvas.drawLine(x - width2, (float) ((x - width2) - radius), x
					+ width2, (float) ((x + width2) - radius), mOutlinePaint);
			canvas.drawPath(mPath, mOutlinePaint);// border
		}

		if (blurBitmap != null)
			canvas.drawBitmap(blurBitmap, 0, 0, null);
		blackPaint.setColor(Color.rgb(0, 0, 0));
		blackPaint.setStyle(Paint.Style.FILL);
		blackPaint.setAntiAlias(false);
		blackPaint.setDither(true);

		canvas.drawRect(0, 0, mParentWidth,
				((mParentHeight - (float) imageBitmapRect.height()) / 2) + 1,
				blackPaint);
		canvas.drawRect(0,
				((mParentHeight + (float) imageBitmapRect.height()) / 2) - 1,
				mParentWidth, mParentHeight, blackPaint);
		canvas.drawRect(0, 0,
				((mParentWidth - (float) imageBitmapRect.width()) / 2),
				mParentHeight, blackPaint);
		canvas.drawRect(((mParentWidth + (float) imageBitmapRect.width()) / 2),
				0, mParentWidth, mParentHeight, blackPaint);

		// Log.d("animate", "imageRect = (" + mImageRect.left + ", 0, " +
		// mImageRect.right + ", " + mImageRect.top);
	}

	public void setMode(Mode mode) {
		if (mode != mMode) {
			mMode = mode;
		}
	}

	final float hysteresis = 30F;

	public void handleMotion(int edge, float dx, float dy) {
		computeLayout(false, tmpRect4);
		if (edge == MOVE) {
			moveBy(dx * (mFocusRect.width() / tmpRect4.width()), dy
					* (mFocusRect.height() / tmpRect4.height()));
		}
	}

	double calculateDy(double dx, double dy) {
		double ndy = dy;
		if (dx != 0) {
			ndy = dx;
			if (dy != 0) {
				if (dy > 0) {
					ndy = Math.abs(ndy);
				} else {
					ndy = Math.abs(ndy) * -1;
				}
			}
			dy = ndy;
		}
		return ndy;
	}

	double calculateDx(double dy, double dx) {
		double ndx = dx;
		if (dy != 0) {
			ndx = dy;
			if (dx != 0) {
				if (dx > 0) {
					ndx = Math.abs(ndx);
				} else {
					ndx = Math.abs(ndx) * -1;
				}
			}
			dx = ndx;
		}
		return ndx;
	}

	public void growBy(final float dx, final float dy, boolean checkMinSize) {
		final RectD r = new RectD(mFocusRect);

		r.inset(-dx, -dy);

		Rect testRect = new Rect();
		getDisplayRect(mMatrix, r, testRect);

		if ((r.width() >= mMinSize) && (r.height() >= mMinSize)
				&& (r.width() <= 2.5 * mImageRect.width())/*
														 * &&
														 * (mImageRect.contains(
														 * r ))
														 */) {
			mFocusRect.set(r);
		}
		invalidate();
	}

	void moveBy(double dx, double dy) {
		moveBy((float) dx, (float) dy);
	}

	void moveBy(float dx, float dy) {
		tmpRectMotion.set(mDrawRect);
		mFocusRect.offset(dx, dy);

		computeLayout(false, mDrawRect);

		tmpRectMotion.union(mDrawRect);
		tmpRectMotion.inset(-dWidth * 2, -dHeight * 2);
	}

	public Rect getInvalidateRect() {
		return tmpRectMotion;
	}

	protected float getScale() {
		float values[] = new float[9];
		mMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	private void adjustFocusRect(RectD r) {

		if (r.left + (r.width() / 2) < mImageRect.left) {
			r.offset(mImageRect.left - r.left - r.width() / 2, 0.0);
		} else if (r.right - (r.width() / 2) > mImageRect.right) {
			r.offset(-(r.right - mImageRect.right - r.width() / 2), 0);
		}

		if (r.top + r.height() / 2 < mImageRect.top) {
			r.offset(0, mImageRect.top - r.top - r.height() / 2);
		} else if (r.bottom - (r.height() / 2) > mImageRect.bottom) {
			r.offset(0, -(r.bottom - mImageRect.bottom - r.height() / 2));
		}

		r.sort();
	}

	private void computeLayout(boolean adjust, Rect outRect) {
		if (adjust) {
			adjustFocusRect(mFocusRect);
		}
		getDisplayRect(mMatrix, mFocusRect, outRect);
	}

	public void getDisplayRect(Matrix m, RectD supportRect, Rect outRect) {
		tmpDisplayRectF.set((float) supportRect.left, (float) supportRect.top,
				(float) supportRect.right, (float) supportRect.bottom);
		m.mapRect(tmpDisplayRectF);
		outRect.set(Math.round(tmpDisplayRectF.left),
				Math.round(tmpDisplayRectF.top),
				Math.round(tmpDisplayRectF.right),
				Math.round(tmpDisplayRectF.bottom));
	}

	public void invalidate() {
		computeLayout(true, mDrawRect);
	}

	public boolean isRunning() {
		return mRunning;
	}

	public void animateAppear(final View parent, Matrix m, RectD imageRect,
			RectD cropRect, final int appearMode) {
		Log.d("animate", "animateAppear");
		if (true/* mRunning */) {
			mRunning = true;
			setMode(Mode.None);
			// parent.postInvalidate();

			if (appearMode == APPEAR_MODE_UP) {

				stopAppear = true;
			}
			if (appearMode == APPEAR_MODE_DOWN) {

				stopAppear = true;
			}

			if (appearMode == APPEAR_MODE_FIRST) {
				mMatrix = new Matrix(m);
				mFocusRect = cropRect;
				mImageRect = new RectD(imageRect);

				mOutlineFill.setAlpha(0);
				mOutlinePaint.setAlpha(0);

				final Rect newRect = new Rect();
				computeLayout(false, newRect);
				stopAppear = false;
			}

			/* if (appearMode == APPEAR_MODE_FIRST) { */
			if (!downInProgress) {
				Log.v("MODE", "appMode=" + appearMode);
				long startTime = System.currentTimeMillis();
				mHandler.post(new Animation(parent, appearMode, startTime));
			} else {
				if (animationQueue.peek() == null) {
					long startTime = System.currentTimeMillis();

					animationQueue.add(new Animation(parent, appearMode,
							startTime));

				}
			}

		}
	}

	private class Animation implements Runnable {

		private View parent;
		private int appearMode;
		private long startTime;
		private boolean first = true;

		public Animation(View parent, int appearMode, long startTime) {
			this.parent = parent;
			this.appearMode = appearMode;
			this.startTime = startTime;
		}

		public int getAppearMode() {
			return appearMode;
		}

		@Override
		public void run() {

			long now = System.currentTimeMillis();
			double currentMs = 0;
			if (first && appearMode != APPEAR_MODE_FIRST) {

				startTime = System.currentTimeMillis();
			}
			if (appearMode == APPEAR_MODE_FIRST) {
				currentMs = Math
						.min(animationAppearDurationMs, now - startTime);
			} else {
				currentMs = Math.min(animationDurationMs, now - startTime);
			}
			int alpha = 0;
			int allalpha = 0;
			if (appearMode == APPEAR_MODE_UP) {
				animationQueue.clear();
				if (first) {

					startTime = System.currentTimeMillis();
					/*
					 * mOutlineFill.setAlpha(153); mOutlinePaint.setAlpha(255);
					 */
					first = false;
					now = System.currentTimeMillis();
				}

				alpha = 153 - (int) Math.round((currentMs * 153)
						/ animationDurationMs);// 255
				allalpha = 255 - (int) Math.round((currentMs * 255)
						/ animationDurationMs);// 255
			}
			if (appearMode == APPEAR_MODE_DOWN) {

				downInProgress = true;
				if (first) {

					mOutlineFill.setAlpha(0);
					mOutlinePaint.setAlpha(0);
					first = false;
				}
				alpha = (int) Math.round((currentMs * 153)
						/ animationDurationMs);// 255
				allalpha = (int) Math.round((currentMs * 255)
						/ animationDurationMs);// 255
			}
			if (appearMode == APPEAR_MODE_FIRST) {
				Log.d("ANIM", "APPEAR_MODE_FIRST");
				if (currentMs < animationAppearDurationMs / 2) {
					alpha = (int) Math.round((currentMs * 153)
							/ (animationAppearDurationMs / 2));// 255
					allalpha = (int) Math.round((currentMs * 255)
							/ (animationAppearDurationMs / 2));// 255
				} else {
					alpha = 153 - (int) Math.round((currentMs * 153)
							/ animationAppearDurationMs);// 255
					allalpha = 255 - (int) Math.round((currentMs * 255)
							/ animationAppearDurationMs);// 255
				}
			}

			mOutlineFill.setAlpha(alpha);
			mOutlinePaint.setAlpha(allalpha);

			if ((appearMode == APPEAR_MODE_FIRST) && (stopAppear)) {
				mRunning = false;
				invalidate();

				if (null != parent) {
					parent.postInvalidate();
				}
			} else if (((currentMs < animationAppearDurationMs) && (appearMode == APPEAR_MODE_FIRST))
					|| ((currentMs < animationDurationMs) && (appearMode == APPEAR_MODE_UP))
					|| ((currentMs < animationDurationMs) && (appearMode == APPEAR_MODE_DOWN))) {
				if (null != parent) {
					parent.invalidate();
					mHandler.post(this);
				}
			} else {
				Log.d("ANIMATION", "END");
				downInProgress = false;
				mRunning = false;
				// invalidate();

				if (null != parent) {
					parent.invalidate();
				}
				if (animationQueue.peek() != null) {

					mHandler.post(animationQueue.poll());
				}
			}

		}

	}

	public void setup(Matrix m, RectD imageRect, RectF imageBitmapRect,
			RectD cropRect) {
		if (this.imageBitmapRect == null)
			this.imageBitmapRect = new RectF(imageBitmapRect);
		mMatrix = new Matrix(m);
		mFocusRect = cropRect;
		mImageRect = new RectD(imageRect);

		computeLayout(true, mDrawRect);

		mOutlineFill.setColor(mOutsideFillColor);
		mOutlineFill.setAlpha(153);
		mOutlineFill.setStyle(Paint.Style.FILL);
		mOutlineFill.setAntiAlias(false);
		// mOutlineFill.setDither(true);
		try {
			ReflectionUtils.invokeMethod(mOutlineFill, "setHinting",
					new Class<?>[] { int.class }, 0);
		} catch (ReflectionException e) {
		}

		setMode(Mode.None);
	}

	public void update(Matrix imageMatrix, Rect imageRect) {
		mMatrix = new Matrix(imageMatrix);
		mImageRect = new RectD(imageRect);
		computeLayout(true, mDrawRect);
	}

	public Matrix getMatrix() {
		return mMatrix;
	}

	public Rect getDrawRect() {
		return mDrawRect;
	}

	public RectD getCropRectD() {
		return mFocusRect;
	}

	public Rect getCropRect() {
		return new Rect((int) mFocusRect.left, (int) mFocusRect.top,
				(int) mFocusRect.right, (int) mFocusRect.bottom);
	}

	public void onSizeChanged(CropImageView cropImageView, int w, int h,
			int oldw, int oldh) {
		cropImageView.getDrawingRect(mViewDrawingRect);
		mParentWidth = w;
		mParentHeight = h;
		blurBitmap = Bitmap.createBitmap(mParentWidth, mParentHeight,
				Config.ARGB_8888);
		if (ereasCanvas != null) {
			ereasCanvas.setBitmap(blurBitmap);
		}
	}

	public void onSizeChanged(FocusImageView focusImageView, int w, int h,
			int oldw, int oldh) {
		focusImageView.getDrawingRect(mViewDrawingRect);
		mParentWidth = w;
		mParentHeight = h;
	}

	public double getAngle() {
		return mAngle;
	}

	public void setAngle(double Angle) {
		this.mAngle = Angle;
	}

	class RecycleBitmap extends Thread {

		private Bitmap b;

		public RecycleBitmap(Bitmap b) {
			this.b = b;
		}

		@Override
		public void run() {
			b.recycle();
		}

	}
}