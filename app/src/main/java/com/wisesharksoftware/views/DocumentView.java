package com.wisesharksoftware.views;

import net.pocketmagic.android.ccdyngridview.DragController;
import net.pocketmagic.android.ccdyngridview.DragSource;
import net.pocketmagic.android.ccdyngridview.DragView;
import net.pocketmagic.android.ccdyngridview.DropTarget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class DocumentView extends FrameLayout implements DragSource,
		DropTarget {

	public DocumentView(Context context) {
		this(context, null, 0);
	}

	public DocumentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DocumentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean onDown(MotionEvent e) {
		Log.e("bla", "ondown");
		return true;
	}

	public boolean onSingleTapUp(MotionEvent e) {
		Log.e("bla", "onSingleTapUp");
		return true;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowDrag() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		
	}

}
