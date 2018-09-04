package com.wisesharksoftware.sticker;

import android.widget.FrameLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class DragLayer extends FrameLayout {
	DragControllerService mDragController;

	public DragLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setDragController(DragControllerService controller) {
		this.mDragController = controller;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (this.mDragController != null) {
			return this.mDragController.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (this.mDragController != null)
			return this.mDragController.onInterceptTouchEvent(ev);
		return false;
	}

	public boolean onTouchEvent(MotionEvent ev) {
		if (this.mDragController != null)
			return this.mDragController.onTouchEvent(ev);
		return false;
	}

	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (this.mDragController != null)
			return this.mDragController.dispatchUnhandledMove(focused,
					direction);
		return false;
	}
}