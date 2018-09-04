package com.wisesharksoftware.sticker;

import com.aviary.android.feather.library.services.drag.DragView;

import android.graphics.Rect;

public abstract interface DropTarget {
	public abstract void onDrop(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject);

	public abstract void onDragEnter(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject);

	public abstract void onDragOver(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject);

	public abstract void onDragExit(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject);

	public abstract boolean acceptDrop(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject);

	public abstract Rect estimateDropLocation(
			DragControllerService.DragSource paramDragSource, int paramInt1,
			int paramInt2, int paramInt3, int paramInt4,
			DragView paramDragView, Object paramObject, Rect paramRect);

	public abstract void getHitRect(Rect paramRect);

	public abstract void getLocationOnScreen(int[] paramArrayOfInt);

	public abstract int getLeft();

	public abstract int getTop();

	public static abstract interface DropTargetListener {
		public abstract boolean acceptDrop(
				DragControllerService.DragSource paramDragSource,
				int paramInt1, int paramInt2, int paramInt3, int paramInt4,
				DragView paramDragView, Object paramObject);

		public abstract void onDrop(
				DragControllerService.DragSource paramDragSource,
				int paramInt1, int paramInt2, int paramInt3, int paramInt4,
				DragView paramDragView, Object paramObject);
	}
}