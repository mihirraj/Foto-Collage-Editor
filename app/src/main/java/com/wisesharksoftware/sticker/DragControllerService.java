package com.wisesharksoftware.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.services.drag.DragView;
import java.util.ArrayList;

public class DragControllerService extends BaseContextService {
	public static int DRAG_ACTION_MOVE = 0;

	public static int DRAG_ACTION_COPY = 1;
	private static final int VIBRATE_DURATION = 35;
	private Vibrator mVibrator;
	private Rect mRectTemp = new Rect();
	private final int[] mCoordinatesTemp = new int[2];
	private float mMotionDownX;
	private float mMotionDownY;
	private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
	private View mOriginator;
	private float mTouchOffsetX;
	private float mTouchOffsetY;
	private DragSource mDragSource;
	private Object mDragInfo;
	private DragView mDragView;
	private ArrayList<DropTarget> mDropTargets = new ArrayList();
	private DragListener mListener;
	private IBinder mWindowToken;
	private View mMoveTarget;
	private DropTarget mLastDropTarget;
	private InputMethodManager mInputMethodManager;
	private DragLayer mDragLayer;

	public DragControllerService(Context context, DragLayer dragLayer) {
		super(context);
		mDragLayer = dragLayer;
	}

	public void activate() {
		this.logger.info(new Object[] { "activate" });
		mDragLayer.setDragController(this);
		this.mWindowToken = mDragLayer.getWindowToken();
	}

	public void deactivate() {
		this.logger.info(new Object[] { "deactivate" });
		mDragLayer.setDragController(null);
		this.mWindowToken = null;
	}

	public boolean active() {
		return (this.mWindowToken != null);
	}

	public boolean startDrag(View v, DragSource source, Object dragInfo,
			int dragAction, boolean animate) {
		Bitmap b = getViewBitmap(v);
		return startDrag(v, b, 0, 0, source, dragInfo, dragAction, animate);
	}

	public boolean startDrag(View v, Bitmap bitmap, int offsetX, int offsetY,
			DragSource source, Object dragInfo, int dragAction, boolean animate) {
		this.mOriginator = v;

		if (bitmap == null) {
			return false;
		}

		int[] loc = this.mCoordinatesTemp;
		v.getLocationOnScreen(loc);
		int screenX = loc[0] + offsetX;
		int screenY = loc[1] + offsetY;

		boolean result = startDrag(bitmap, screenX, screenY, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), source, dragInfo,
				dragAction, animate);

		if ((dragAction == DRAG_ACTION_MOVE) && (result)) {
			v.setVisibility(8);
		}

		return result;
	}

	public boolean startDrag(Bitmap b, int screenX, int screenY,
			int textureLeft, int textureTop, int textureWidth,
			int textureHeight, DragSource source, Object dragInfo,
			int dragAction, boolean animate) {
		if (this.mWindowToken == null) {
			this.logger
					.error(new Object[] { "window token is null. drag will not start!" });
			return false;
		}

		if (!(this.mWindowToken.pingBinder()))
			return false;

		if (this.mInputMethodManager == null) {
			this.mInputMethodManager = ((InputMethodManager)getBaseContext().getSystemService("input_method"));
		}
		this.mInputMethodManager.hideSoftInputFromWindow(this.mWindowToken, 0);

		if (this.mListener != null) {
			this.mListener.onDragStart(source, dragInfo, dragAction);
		}

		int registrationX = (int) this.mMotionDownX - screenX;
		int registrationY = (int) this.mMotionDownY - screenY;

		this.mTouchOffsetX = (this.mMotionDownX - screenX);
		this.mTouchOffsetY = (this.mMotionDownY - screenY);

		this.mDragSource = source;
		this.mDragInfo = dragInfo;

		if (this.mVibrator != null) {
			this.mVibrator.vibrate(35L);
		}

		this.mDragView = new DragView(getBaseContext(), b,
				registrationX, registrationY, textureLeft, textureTop,
				textureWidth, textureHeight);
		this.mDragView.show(this.mWindowToken, (int) this.mMotionDownX,
				(int) this.mMotionDownY, animate);
		return true;
	}

	public boolean isDragging() {
		return (this.mDragView != null);
	}

	private Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);

		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();

		if (cacheBitmap == null) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		return isDragging();
	}

	public void cancelDrag() {
		endDrag();
	}

	private void endDrag() {
		if (isDragging()) {
			if (this.mOriginator != null) {
				this.mOriginator.setVisibility(0);
			}

			boolean animate = true;

			if (this.mListener != null) {
				animate = this.mListener.onDragEnd();
			}

			if (this.mDragView != null) {
				this.mDragView.remove(animate);
				this.mDragView = null;
			}
		}
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!(active()))
			return false;

		int action = ev.getAction();

		if (action == 0) {
			recordScreenSize();
		}

		int screenX = clamp((int) ev.getRawX(), 0,
				this.mDisplayMetrics.widthPixels);
		int screenY = clamp((int) ev.getRawY(), 0,
				this.mDisplayMetrics.heightPixels);

		switch (action) {
		case 2:
			break;
		case 0:
			this.mMotionDownX = screenX;
			this.mMotionDownY = screenY;
			this.mLastDropTarget = null;
			break;
		case 1:
			if (isDragging()) {
				drop(screenX, screenY);
			}
			endDrag();
			break;
		case 3:
			cancelDrag();
		}

		return isDragging();
	}

	public void setMoveTarget(View view) {
		this.mMoveTarget = view;
	}

	public boolean dispatchUnhandledMove(View focused, int direction) {
		return ((this.mMoveTarget != null) && (this.mMoveTarget
				.dispatchUnhandledMove(focused, direction)));
	}

	public boolean onTouchEvent(MotionEvent ev) {
		if (!(isDragging())) {
			return false;
		}

		int action = ev.getAction();
		int screenX = clamp((int) ev.getRawX(), 0,
				this.mDisplayMetrics.widthPixels);
		int screenY = clamp((int) ev.getRawY(), 0,
				this.mDisplayMetrics.heightPixels);

		switch (action) {
		case 0:
			this.mMotionDownX = screenX;
			this.mMotionDownY = screenY;
			break;
		case 2:
			this.mDragView.move((int) ev.getRawX(), (int) ev.getRawY());

			int[] coordinates = this.mCoordinatesTemp;
			DropTarget dropTarget = findDropTarget(screenX, screenY,
					coordinates);
			if (dropTarget != null) {
				boolean valid = dropTarget.acceptDrop(this.mDragSource,
						coordinates[0], coordinates[1],
						(int) this.mTouchOffsetX, (int) this.mTouchOffsetY,
						this.mDragView, this.mDragInfo);
				if (!(valid)) {
					dropTarget = null;
				}

				if (this.mLastDropTarget == dropTarget) {
					if (dropTarget != null)
						dropTarget.onDragOver(this.mDragSource, coordinates[0],
								coordinates[1], (int) this.mTouchOffsetX,
								(int) this.mTouchOffsetY, this.mDragView,
								this.mDragInfo);
				} else {
					if (this.mLastDropTarget != null) {
						this.mLastDropTarget.onDragExit(this.mDragSource,
								coordinates[0], coordinates[1],
								(int) this.mTouchOffsetX,
								(int) this.mTouchOffsetY, this.mDragView,
								this.mDragInfo);
					}

					if (dropTarget != null) {
						dropTarget.onDragEnter(this.mDragSource,
								coordinates[0], coordinates[1],
								(int) this.mTouchOffsetX,
								(int) this.mTouchOffsetY, this.mDragView,
								this.mDragInfo);
					}
				}
			} else if (this.mLastDropTarget != null) {
				this.mLastDropTarget.onDragExit(this.mDragSource,
						coordinates[0], coordinates[1],
						(int) this.mTouchOffsetX, (int) this.mTouchOffsetY,
						this.mDragView, this.mDragInfo);
			}

			this.mLastDropTarget = dropTarget;
			break;
		case 1:
			if (isDragging()) {
				drop(screenX, screenY);
			}
			endDrag();

			break;
		case 3:
			cancelDrag();
		}

		return true;
	}

	private boolean drop(float x, float y) {
		int[] coordinates = this.mCoordinatesTemp;
		DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

		if (dropTarget != null) {
			dropTarget.onDragExit(this.mDragSource, coordinates[0],
					coordinates[1], (int) this.mTouchOffsetX,
					(int) this.mTouchOffsetY, this.mDragView, this.mDragInfo);
			if (dropTarget.acceptDrop(this.mDragSource, coordinates[0],
					coordinates[1], (int) this.mTouchOffsetX,
					(int) this.mTouchOffsetY, this.mDragView, this.mDragInfo)) {
				dropTarget.onDrop(this.mDragSource, coordinates[0],
						coordinates[1], (int) this.mTouchOffsetX,
						(int) this.mTouchOffsetY, this.mDragView,
						this.mDragInfo);
				this.mDragSource.onDropCompleted((View) dropTarget, true);
				return true;
			}
			this.mDragSource.onDropCompleted((View) dropTarget, false);
			return true;
		}

		return false;
	}

	private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
		Rect r = this.mRectTemp;

		ArrayList dropTargets = this.mDropTargets;
		int count = dropTargets.size();
		for (int i = count - 1; i >= 0; --i) {
			DropTarget target = (DropTarget) dropTargets.get(i);
			target.getHitRect(r);
			target.getLocationOnScreen(dropCoordinates);
			r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1]
					- target.getTop());
			if (r.contains(x, y)) {
				dropCoordinates[0] = (x - dropCoordinates[0]);
				dropCoordinates[1] = (y - dropCoordinates[1]);
				return target;
			}
		}
		return null;
	}

	private void recordScreenSize() {
		((WindowManager) getBaseContext().getSystemService(
				"window")).getDefaultDisplay().getMetrics(this.mDisplayMetrics);
	}

	private static int clamp(int val, int min, int max) {
		if (val < min)
			return min;
		if (val >= max) {
			return (max - 1);
		}
		return val;
	}

	public void setWindowToken(IBinder token) {
		this.mWindowToken = token;
	}

	public void setDragListener(DragListener l) {
		this.mListener = l;
	}

	public void addDropTarget(DropTarget target) {
		this.mDropTargets.add(target);
	}

	public void removeDropTarget(DropTarget target) {
		this.mDropTargets.remove(target);
	}

	public void dispose() {
		deactivate();
		this.mDropTargets.clear();
		this.mListener = null;
		this.mWindowToken = null;
	}

	public static abstract interface DragListener {
		public abstract void onDragStart(
				DragControllerService.DragSource paramDragSource,
				Object paramObject, int paramInt);

		public abstract boolean onDragEnd();
	}

	public static abstract interface DragSource {
		public abstract void setDragController(
				DragControllerService paramDragControllerService);

		public abstract DragControllerService getDragController();

		public abstract void onDropCompleted(View paramView,
				boolean paramBoolean);
	}
}